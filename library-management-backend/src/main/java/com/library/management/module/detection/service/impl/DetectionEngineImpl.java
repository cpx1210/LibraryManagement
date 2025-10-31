package com.library.management.module.detection.service.impl;

import com.library.management.module.detection.dto.BookItemDTO;
import com.library.management.module.detection.dto.DetectionResultDTO;
import com.library.management.module.detection.service.DetectionEngine;
import com.library.management.module.problembook.entity.ProblemBook;
import com.library.management.module.problembook.mapper.ProblemBookMapper;
import com.library.management.module.publisher.entity.PublisherWhitelist;
import com.library.management.module.publisher.mapper.PublisherWhitelistMapper;
import com.library.management.module.sensitiveword.entity.SensitiveWords;
import com.library.management.module.sensitiveword.mapper.SensitiveWordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检测引擎实现类
 *
 * 采用方案A：PostgreSQL 全文检索
 *
 * 检测流程：
 * 1. 敏感词检测（书名 + 作者）- 使用 ILIKE 模糊匹配
 * 2. 问题书目检测（ISBN 精确 + 书名相似度）- 使用 similarity() 函数
 * 3. 出版社白名单检测（出版社名称精确匹配）
 * 4. 根据检测结果计算风险等级
 *
 * 风险等级优先级：
 * - 命中敏感词 -> high（红色）
 * - 命中问题书目 -> medium（黄色）
 * - 非白名单出版社 -> low（无标注）
 */
@Slf4j
@Service
public class DetectionEngineImpl implements DetectionEngine {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Resource
    private ProblemBookMapper problemBookMapper;

    @Resource
    private PublisherWhitelistMapper publisherWhitelistMapper;

    /**
     * 检测单本书
     *
     * @param book 书目信息
     * @return 检测结果
     */
    @Override
    public DetectionResultDTO detectBook(BookItemDTO book) {
        log.debug("开始检测书目：{}", book.getBookName());

        DetectionResultDTO result = DetectionResultDTO.builder()
                .isbn(book.getIsbn())
                .bookName(book.getBookName())
                .subtitle(book.getSubtitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publishLocation(book.getPublishLocation())
                .targetAudience(book.getTargetAudience())
                .contentSummary(book.getContentSummary())
                .hitSensitive(false)
                .hitProblemBook(false)
                .isWhitelistPublisher(false)
                .sensitiveWords(new ArrayList<>())
                .build();

        // 1. 敏感词检测（扩展的字段检测）
        detectSensitiveWords(result);

        // 2. 问题书目检测
        detectProblemBook(result);

        // 3. 出版社白名单检测
        checkPublisherWhitelist(result);

        // 4. 计算综合风险等级
        result.calculateRiskLevel();

        // 5. 生成检测备注
        generateRemark(result);

        log.debug("检测完成：{}，风险等级：{}", book.getBookName(), result.getRiskLevel());

        return result;
    }

    /**
     * 批量检测书目
     *
     * @param books 书目列表
     * @return 检测结果列表
     */
    @Override
    public List<DetectionResultDTO> batchDetect(List<BookItemDTO> books) {
        log.info("开始批量检测，共 {} 本书", books.size());

        List<DetectionResultDTO> results = new ArrayList<>(books.size());

        for (BookItemDTO book : books) {
            try {
                DetectionResultDTO result = detectBook(book);
                results.add(result);
            } catch (Exception e) {
                log.error("检测书目失败：{}，错误：{}", book.getBookName(), e.getMessage(), e);
                // 创建一个错误结果
                DetectionResultDTO errorResult = DetectionResultDTO.builder()
                        .isbn(book.getIsbn())
                        .bookName(book.getBookName())
                        .author(book.getAuthor())
                        .publisher(book.getPublisher())
                        .hitSensitive(false)
                        .hitProblemBook(false)
                        .isWhitelistPublisher(false)
                        .riskLevel("low")
                        .remark("检测失败：" + e.getMessage())
                        .build();
                results.add(errorResult);
            }
        }

        log.info("批量检测完成，共检测 {} 本书", results.size());

        return results;
    }

    /**
     * 敏感词检测（优化版）
     * 根据敏感词的检测类型（关键词、书名、作者）进行精确检测
     *
     * 改进点：
     * 1. 扩展"关键词"类型的检测字段（书名、副题名、作者、内容简介、读者对象等）
     * 2. 支持 & 符号的 AND 逻辑（如"警察&腐败"需要同时包含两个词）
     * 3. 书名和作者采用更精确的匹配方式
     *
     * @param result 检测结果（会被修改）
     */
    private void detectSensitiveWords(DetectionResultDTO result) {
        List<SensitiveWords> hitWords = new ArrayList<>();

        // 1. 检测"关键词"类型（全局检测：所有文本字段）
        List<SensitiveWords> keywordTypeWords = sensitiveWordMapper.detectSensitiveWordsByType("关键词");
        if (keywordTypeWords != null && !keywordTypeWords.isEmpty()) {
            for (SensitiveWords word : keywordTypeWords) {
                String keyword = word.getKeyword();
                boolean hit = false;
                String hitField = "";

                // 收集所有需要检测的文本字段
                List<String> fieldsToCheck = new ArrayList<>();
                if (StringUtils.hasText(result.getBookName())) fieldsToCheck.add(result.getBookName());
                if (StringUtils.hasText(result.getSubtitle())) fieldsToCheck.add(result.getSubtitle());
                if (StringUtils.hasText(result.getAuthor())) fieldsToCheck.add(result.getAuthor());
                if (StringUtils.hasText(result.getPublisher())) fieldsToCheck.add(result.getPublisher());
                if (StringUtils.hasText(result.getPublishLocation())) fieldsToCheck.add(result.getPublishLocation());
                if (StringUtils.hasText(result.getTargetAudience())) fieldsToCheck.add(result.getTargetAudience());
                if (StringUtils.hasText(result.getContentSummary())) fieldsToCheck.add(result.getContentSummary());

                // 检测每个字段
                for (String fieldText : fieldsToCheck) {
                    if (matchKeyword(fieldText, keyword)) {
                        hit = true;
                        hitField = fieldText.length() > 20 ? fieldText.substring(0, 20) + "..." : fieldText;
                        break;
                    }
                }

                if (hit) {
                    hitWords.add(word);
                    log.debug("命中关键词类型敏感词：「{}」，命中内容：{}", keyword, hitField);
                }
            }
        }

        // 2. 检测"书名"类型（仅检测书名和副题名字段，使用精确匹配）
        List<SensitiveWords> bookNameTypeWords = sensitiveWordMapper.detectSensitiveWordsByType("书名");
        if (bookNameTypeWords != null && !bookNameTypeWords.isEmpty()) {
            for (SensitiveWords word : bookNameTypeWords) {
                String keyword = word.getKeyword();
                boolean hit = false;

                // 检测书名（精确匹配或包含匹配）
                if (StringUtils.hasText(result.getBookName())) {
                    if (result.getBookName().equals(keyword) || result.getBookName().contains(keyword)) {
                        hit = true;
                    }
                }

                // 检测副题名
                if (!hit && StringUtils.hasText(result.getSubtitle())) {
                    if (result.getSubtitle().equals(keyword) || result.getSubtitle().contains(keyword)) {
                        hit = true;
                    }
                }

                if (hit) {
                    hitWords.add(word);
                    log.debug("书名「{}」命中书名类型敏感词：{}", result.getBookName(), keyword);
                }
            }
        }

        // 3. 检测"作者"类型（仅检测作者字段，处理多作者情况）
        List<SensitiveWords> authorTypeWords = sensitiveWordMapper.detectSensitiveWordsByType("作者");
        if (authorTypeWords != null && !authorTypeWords.isEmpty()) {
            for (SensitiveWords word : authorTypeWords) {
                String keyword = word.getKeyword();
                boolean hit = false;

                // 检测作者字段（合并的作者字段）
                if (StringUtils.hasText(result.getAuthor())) {
                    // 分割多个作者（支持 ; , 、 等分隔符）
                    String[] authors = result.getAuthor().split("[;,、]");
                    for (String author : authors) {
                        String trimmedAuthor = author.trim();
                        // 精确匹配或包含匹配
                        if (trimmedAuthor.equals(keyword) || trimmedAuthor.contains(keyword)) {
                            hit = true;
                            break;
                        }
                    }
                }

                if (hit) {
                    hitWords.add(word);
                    log.debug("作者「{}」命中作者类型敏感词：{}", result.getAuthor(), keyword);
                }
            }
        }

        // 设置检测结果
        if (!hitWords.isEmpty()) {
            result.setHitSensitive(true);

            // 提取敏感词关键字列表（去重）
            List<String> keywords = hitWords.stream()
                    .map(SensitiveWords::getKeyword)
                    .distinct()
                    .collect(Collectors.toList());
            result.setSensitiveWords(keywords);

            // 获取最高风险等级
            int maxRiskLevel = hitWords.stream()
                    .mapToInt(SensitiveWords::getRiskLevel)
                    .max()
                    .orElse(1);
            result.setMaxSensitiveRiskLevel(maxRiskLevel);

            log.info("命中敏感词：{}，最高风险等级：{}", keywords, maxRiskLevel);
        }
    }

    /**
     * 关键词匹配（支持 & 符号的 AND 逻辑）
     *
     * @param text 待检测文本
     * @param keyword 关键词（可能包含 & 符号）
     * @return 是否匹配
     */
    private boolean matchKeyword(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }

        // 处理 & 符号（表示 AND 逻辑：必须同时包含所有部分）
        if (keyword.contains("&")) {
            String[] parts = keyword.split("&");
            for (String part : parts) {
                String trimmedPart = part.trim();
                if (!text.contains(trimmedPart)) {
                    return false;  // 任何一个部分不存在，都不算匹配
                }
            }
            return true;  // 所有部分都存在
        } else {
            // 普通匹配
            return text.contains(keyword);
        }
    }

    /**
     * 问题书目检测
     * 使用 ISBN 精确匹配 + 书名相似度匹配
     *
     * @param result 检测结果（会被修改）
     */
    private void detectProblemBook(DetectionResultDTO result) {
        // ISBN 和书名至少有一个不为空才进行检测
        if (!StringUtils.hasText(result.getIsbn()) && !StringUtils.hasText(result.getBookName())) {
            return;
        }

        ProblemBook problemBook = problemBookMapper.detectProblemBook(
                result.getIsbn(),
                result.getBookName()
        );

        if (problemBook != null) {
            result.setHitProblemBook(true);

            // 生成问题书目信息
            StringBuilder problemInfo = new StringBuilder();
            problemInfo.append("问题类型：").append(problemBook.getProblemType());
            if (StringUtils.hasText(problemBook.getSource())) {
                problemInfo.append("，来源：").append(problemBook.getSource());
            }
            result.setProblemBookInfo(problemInfo.toString());

            log.info("命中问题书目：{}，{}", result.getBookName(), problemInfo);
        }
    }

    /**
     * 出版社白名单检测
     * 检查出版社是否在白名单内
     *
     * @param result 检测结果（会被修改）
     */
    private void checkPublisherWhitelist(DetectionResultDTO result) {
        if (!StringUtils.hasText(result.getPublisher())) {
            result.setIsWhitelistPublisher(false);
            return;
        }

        PublisherWhitelist whitelist = publisherWhitelistMapper.selectActiveByPublisherName(
                result.getPublisher()
        );

        if (whitelist != null) {
            result.setIsWhitelistPublisher(true);
            log.debug("出版社「{}」在白名单内", result.getPublisher());
        } else {
            result.setIsWhitelistPublisher(false);
            log.debug("出版社「{}」不在白名单内", result.getPublisher());
        }
    }

    /**
     * 生成检测备注信息
     *
     * @param result 检测结果（会被修改）
     */
    private void generateRemark(DetectionResultDTO result) {
        StringBuilder remark = new StringBuilder();

        // 1. 敏感词信息
        if (Boolean.TRUE.equals(result.getHitSensitive())) {
            remark.append("【敏感词】");
            if (result.getSensitiveWords() != null && !result.getSensitiveWords().isEmpty()) {
                remark.append("命中敏感词：");
                remark.append(String.join("、", result.getSensitiveWords()));
            }
        }

        // 2. 问题书目信息
        if (Boolean.TRUE.equals(result.getHitProblemBook())) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【问题书目】");
            if (StringUtils.hasText(result.getProblemBookInfo())) {
                remark.append(result.getProblemBookInfo());
            }
        }

        // 3. 白名单信息
        if (Boolean.FALSE.equals(result.getIsWhitelistPublisher())) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【非白名单出版社】");
        }

        // 4. 无问题
        if (remark.length() == 0) {
            remark.append("无问题");
        }

        result.setRemark(remark.toString());
    }
}
