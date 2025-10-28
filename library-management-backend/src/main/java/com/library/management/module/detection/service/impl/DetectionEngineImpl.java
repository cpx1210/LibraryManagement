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
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .hitSensitive(false)
                .hitProblemBook(false)
                .isWhitelistPublisher(false)
                .sensitiveWords(new ArrayList<>())
                .build();

        // 1. 敏感词检测（书名 + 作者）
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
     * 敏感词检测
     * 检测书名和作者中是否包含敏感词
     *
     * @param result 检测结果（会被修改）
     */
    private void detectSensitiveWords(DetectionResultDTO result) {
        List<SensitiveWords> hitWords = new ArrayList<>();

        // 检测书名中的敏感词
        if (StringUtils.hasText(result.getBookName())) {
            List<SensitiveWords> bookNameHits = sensitiveWordMapper.detectSensitiveWords(result.getBookName());
            if (bookNameHits != null && !bookNameHits.isEmpty()) {
                hitWords.addAll(bookNameHits);
                log.debug("书名「{}」命中 {} 个敏感词", result.getBookName(), bookNameHits.size());
            }
        }

        // 检测作者中的敏感词
        if (StringUtils.hasText(result.getAuthor())) {
            List<SensitiveWords> authorHits = sensitiveWordMapper.detectSensitiveWords(result.getAuthor());
            if (authorHits != null && !authorHits.isEmpty()) {
                hitWords.addAll(authorHits);
                log.debug("作者「{}」命中 {} 个敏感词", result.getAuthor(), authorHits.size());
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
