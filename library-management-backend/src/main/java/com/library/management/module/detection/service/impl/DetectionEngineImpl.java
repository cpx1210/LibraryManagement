package com.library.management.module.detection.service.impl;

import com.library.management.module.detection.dto.BookItemDTO;
import com.library.management.module.detection.dto.DetectionReferenceDataSnapshot;
import com.library.management.module.detection.dto.DetectionResultDTO;
import com.library.management.module.detection.dto.ProblemBookMatchCandidate;
import com.library.management.module.detection.dto.SensitiveHitDetailDTO;
import com.library.management.module.detection.service.DetectionEngine;
import com.library.management.module.problembook.entity.ProblemBook;
import com.library.management.module.sensitiveword.entity.SensitiveWords;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 检测引擎实现类。
 *
 * 对于大批量馆藏任务，敏感词、问题书目和出版社白名单统一按任务级快照读取，
 * 避免 140 万册检测过程中产生按册重复查库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionEngineImpl implements DetectionEngine {

    private final DetectionReferenceDataService detectionReferenceDataService;

    @Override
    public DetectionResultDTO detectBook(BookItemDTO book) {
        log.debug("开始检测书目：{}", book != null ? book.getBookName() : null);
        return detectBook(book, detectionReferenceDataService.getSnapshot());
    }

    @Override
    public List<DetectionResultDTO> batchDetect(List<BookItemDTO> books) {
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("开始批量检测，共 {} 本书", books.size());
        DetectionReferenceDataSnapshot referenceData = detectionReferenceDataService.getSnapshot();
        List<DetectionResultDTO> results = new ArrayList<>(books.size());

        for (BookItemDTO book : books) {
            try {
                results.add(detectBook(book, referenceData));
            } catch (Exception e) {
                log.error("检测书目失败：{}，错误：{}", book != null ? book.getBookName() : null, e.getMessage(), e);
                results.add(DetectionResultDTO.builder()
                        .isbn(book != null ? book.getIsbn() : null)
                        .bookName(book != null ? book.getBookName() : null)
                        .author(book != null ? book.getAuthor() : null)
                        .publisher(book != null ? book.getPublisher() : null)
                        .hitSensitive(false)
                        .hitProblemBook(false)
                        .isWhitelistPublisher(false)
                        .riskLevel("low")
                        .remark("检测失败：" + e.getMessage())
                        .build());
            }
        }

        log.info("批量检测完成，共检测 {} 本书", results.size());
        return results;
    }

    private DetectionResultDTO detectBook(BookItemDTO book, DetectionReferenceDataSnapshot referenceData) {
        if (book == null) {
            return DetectionResultDTO.builder()
                    .hitSensitive(false)
                    .hitProblemBook(false)
                    .isWhitelistPublisher(false)
                    .sensitiveWords(Collections.emptyList())
                    .build();
        }

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

        detectSensitiveWords(result, referenceData);
        detectProblemBook(result, referenceData);
        checkPublisherWhitelist(result, referenceData);
        result.calculateRiskLevel();
        generateRemark(result);
        return result;
    }

    private void detectSensitiveWords(DetectionResultDTO result, DetectionReferenceDataSnapshot referenceData) {
        List<SensitiveWords> hitWords = new ArrayList<>();
        List<SensitiveHitDetailDTO> hitDetails = new ArrayList<>();

        java.util.Map<String, String> fieldNameMap = new java.util.LinkedHashMap<>();
        if (StringUtils.hasText(result.getBookName())) {
            fieldNameMap.put(result.getBookName(), "书名");
        }
        if (StringUtils.hasText(result.getSubtitle())) {
            fieldNameMap.put(result.getSubtitle(), "副标题");
        }
        if (StringUtils.hasText(result.getAuthor())) {
            fieldNameMap.put(result.getAuthor(), "作者");
        }
        if (StringUtils.hasText(result.getPublisher())) {
            fieldNameMap.put(result.getPublisher(), "出版社");
        }
        if (StringUtils.hasText(result.getPublishLocation())) {
            fieldNameMap.put(result.getPublishLocation(), "出版地");
        }
        if (StringUtils.hasText(result.getTargetAudience())) {
            fieldNameMap.put(result.getTargetAudience(), "读者对象");
        }
        if (StringUtils.hasText(result.getContentSummary())) {
            fieldNameMap.put(result.getContentSummary(), "内容简介");
        }

        detectKeywordTypeSensitiveWords(referenceData.getKeywordSensitiveWords(), fieldNameMap, hitWords, hitDetails);
        detectBookNameTypeSensitiveWords(referenceData.getBookNameSensitiveWords(), result, hitWords, hitDetails);
        detectAuthorTypeSensitiveWords(referenceData.getAuthorSensitiveWords(), result, hitWords, hitDetails);

        if (!hitWords.isEmpty()) {
            result.setHitSensitive(true);
            result.setSensitiveWords(hitWords.stream()
                    .map(SensitiveWords::getKeyword)
                    .distinct()
                    .collect(Collectors.toList()));
            result.setSensitiveHitDetails(hitDetails);
            result.setMaxSensitiveRiskLevel(hitWords.stream()
                    .mapToInt(SensitiveWords::getRiskLevel)
                    .max()
                    .orElse(1));
        }
    }

    private void detectKeywordTypeSensitiveWords(List<SensitiveWords> words,
                                                 java.util.Map<String, String> fieldNameMap,
                                                 List<SensitiveWords> hitWords,
                                                 List<SensitiveHitDetailDTO> hitDetails) {
        if (words == null || words.isEmpty()) {
            return;
        }

        for (SensitiveWords word : words) {
            String keyword = word.getKeyword();
            for (java.util.Map.Entry<String, String> entry : fieldNameMap.entrySet()) {
                if (matchKeyword(entry.getKey(), keyword)) {
                    hitWords.add(word);
                    hitDetails.add(SensitiveHitDetailDTO.builder()
                            .fieldName(entry.getValue())
                            .keyword(keyword)
                            .reason(word.getAlertMessage())
                            .detectionType("关键词")
                            .riskLevel(word.getRiskLevel())
                            .build());
                }
            }
        }
    }

    private void detectBookNameTypeSensitiveWords(List<SensitiveWords> words,
                                                  DetectionResultDTO result,
                                                  List<SensitiveWords> hitWords,
                                                  List<SensitiveHitDetailDTO> hitDetails) {
        if (words == null || words.isEmpty()) {
            return;
        }

        for (SensitiveWords word : words) {
            String keyword = word.getKeyword();
            if (StringUtils.hasText(result.getBookName())
                    && (result.getBookName().equals(keyword) || result.getBookName().contains(keyword))) {
                hitWords.add(word);
                hitDetails.add(SensitiveHitDetailDTO.builder()
                        .fieldName("书名")
                        .keyword(keyword)
                        .reason(word.getAlertMessage())
                        .detectionType("书名")
                        .riskLevel(word.getRiskLevel())
                        .build());
            }

            if (StringUtils.hasText(result.getSubtitle())
                    && (result.getSubtitle().equals(keyword) || result.getSubtitle().contains(keyword))) {
                hitWords.add(word);
                hitDetails.add(SensitiveHitDetailDTO.builder()
                        .fieldName("副标题")
                        .keyword(keyword)
                        .reason(word.getAlertMessage())
                        .detectionType("书名")
                        .riskLevel(word.getRiskLevel())
                        .build());
            }
        }
    }

    private void detectAuthorTypeSensitiveWords(List<SensitiveWords> words,
                                                DetectionResultDTO result,
                                                List<SensitiveWords> hitWords,
                                                List<SensitiveHitDetailDTO> hitDetails) {
        if (words == null || words.isEmpty() || !StringUtils.hasText(result.getAuthor())) {
            return;
        }

        String[] authors = result.getAuthor().split("[;,，、]");
        for (SensitiveWords word : words) {
            String keyword = word.getKeyword();
            for (int i = 0; i < authors.length; i++) {
                String trimmedAuthor = authors[i] == null ? null : authors[i].trim();
                if (!StringUtils.hasText(trimmedAuthor)) {
                    continue;
                }
                if (trimmedAuthor.equals(keyword) || trimmedAuthor.contains(keyword)) {
                    hitWords.add(word);
                    hitDetails.add(SensitiveHitDetailDTO.builder()
                            .fieldName(authors.length > 1 ? "作者" + (i + 1) : "作者")
                            .keyword(keyword)
                            .reason(word.getAlertMessage())
                            .detectionType("作者")
                            .riskLevel(word.getRiskLevel())
                            .build());
                }
            }
        }
    }

    private boolean matchKeyword(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }

        if (keyword.contains("&")) {
            String[] parts = keyword.split("&");
            for (String part : parts) {
                String trimmedPart = part.trim();
                if (!text.contains(trimmedPart)) {
                    return false;
                }
            }
            return true;
        }
        return text.contains(keyword);
    }

    private void detectProblemBook(DetectionResultDTO result, DetectionReferenceDataSnapshot referenceData) {
        if (!StringUtils.hasText(result.getIsbn()) && !StringUtils.hasText(result.getBookName())) {
            return;
        }

        ProblemBook problemBook = matchProblemBook(result, referenceData);
        if (problemBook == null) {
            return;
        }

        result.setHitProblemBook(true);
        StringBuilder problemInfo = new StringBuilder();
        problemInfo.append("问题类型：").append(problemBook.getProblemType());
        if (StringUtils.hasText(problemBook.getSource())) {
            problemInfo.append("，来源：").append(problemBook.getSource());
        }
        result.setProblemBookInfo(problemInfo.toString());
    }

    private ProblemBook matchProblemBook(DetectionResultDTO result, DetectionReferenceDataSnapshot referenceData) {
        String normalizedIsbn = normalizeText(result.getIsbn());
        if (StringUtils.hasText(normalizedIsbn)) {
            ProblemBook exactMatch = referenceData.getProblemBookByIsbn().get(normalizedIsbn);
            if (exactMatch != null) {
                return exactMatch;
            }
        }

        String normalizedBookName = normalizeText(result.getBookName());
        if (!StringUtils.hasText(normalizedBookName)) {
            return null;
        }

        ProblemBook bestMatch = null;
        int bestRank = Integer.MAX_VALUE;
        int bestLength = Integer.MAX_VALUE;

        for (ProblemBookMatchCandidate candidate : referenceData.getProblemBookNameCandidates()) {
            int rank = rankProblemBookName(normalizedBookName, candidate.getNormalizedBookName());
            if (rank == Integer.MAX_VALUE) {
                continue;
            }

            if (rank < bestRank || (rank == bestRank && candidate.getNormalizedBookNameLength() < bestLength)) {
                bestRank = rank;
                bestLength = candidate.getNormalizedBookNameLength();
                bestMatch = candidate.getProblemBook();
            }
        }

        return bestMatch;
    }

    private int rankProblemBookName(String normalizedBookName, String normalizedProblemBookName) {
        if (!StringUtils.hasText(normalizedBookName) || !StringUtils.hasText(normalizedProblemBookName)) {
            return Integer.MAX_VALUE;
        }
        if (normalizedProblemBookName.equals(normalizedBookName)) {
            return 0;
        }
        if (normalizedProblemBookName.startsWith(normalizedBookName)) {
            return 1;
        }
        if (normalizedProblemBookName.contains(normalizedBookName)) {
            return 2;
        }
        return Integer.MAX_VALUE;
    }

    private void checkPublisherWhitelist(DetectionResultDTO result, DetectionReferenceDataSnapshot referenceData) {
        if (!StringUtils.hasText(result.getPublisher())) {
            result.setIsWhitelistPublisher(false);
            return;
        }

        String normalizedPublisher = normalizeText(result.getPublisher());
        result.setIsWhitelistPublisher(referenceData.getActiveWhitelistPublishers().contains(normalizedPublisher));
    }

    private void generateRemark(DetectionResultDTO result) {
        StringBuilder remark = new StringBuilder();

        if (Boolean.FALSE.equals(result.getIsWhitelistPublisher())) {
            remark.append("【非白名单出版社】");
        }

        if (Boolean.TRUE.equals(result.getHitProblemBook())) {
            if (remark.length() > 0) {
                remark.append(" ");
            }
            remark.append("【问题书目】");
            if (StringUtils.hasText(result.getProblemBookInfo())) {
                remark.append(result.getProblemBookInfo());
            }
        }

        if (Boolean.TRUE.equals(result.getHitSensitive())) {
            if (remark.length() > 0) {
                remark.append(" ");
            }
            remark.append("【敏感词】");

            if (result.getSensitiveHitDetails() != null && !result.getSensitiveHitDetails().isEmpty()) {
                List<String> detailTexts = result.getSensitiveHitDetails().stream()
                        .map(detail -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append(detail.getFieldName());
                            sb.append("匹配到关键词：");
                            sb.append(detail.getKeyword());
                            if (StringUtils.hasText(detail.getReason())) {
                                sb.append("（原因：");
                                sb.append(detail.getReason());
                                sb.append("）");
                            }
                            return sb.toString();
                        })
                        .collect(Collectors.toList());
                remark.append(String.join("；", detailTexts));
            } else if (result.getSensitiveWords() != null && !result.getSensitiveWords().isEmpty()) {
                remark.append("命中敏感词：");
                remark.append(String.join("、", result.getSensitiveWords()));
            }
        }

        result.setRemark(remark.toString());
    }

    private String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
