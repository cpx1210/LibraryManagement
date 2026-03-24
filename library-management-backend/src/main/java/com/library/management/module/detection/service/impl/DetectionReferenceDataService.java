package com.library.management.module.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.management.module.detection.dto.DetectionReferenceDataSnapshot;
import com.library.management.module.detection.dto.ProblemBookMatchCandidate;
import com.library.management.module.problembook.entity.ProblemBook;
import com.library.management.module.problembook.mapper.ProblemBookMapper;
import com.library.management.module.publisher.entity.PublisherWhitelist;
import com.library.management.module.publisher.mapper.PublisherWhitelistMapper;
import com.library.management.module.sensitiveword.entity.SensitiveWords;
import com.library.management.module.sensitiveword.mapper.SensitiveWordMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 检测过程依赖的参考数据加载服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionReferenceDataService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final ProblemBookMapper problemBookMapper;
    private final PublisherWhitelistMapper publisherWhitelistMapper;

    @Cacheable(cacheNames = "detectionReferenceSnapshot", key = "'global'")
    public DetectionReferenceDataSnapshot getSnapshot() {
        log.info("加载检测参考数据快照");

        List<SensitiveWords> keywordSensitiveWords = safeList(sensitiveWordMapper.detectSensitiveWordsByType("关键词"));
        List<SensitiveWords> bookNameSensitiveWords = safeList(sensitiveWordMapper.detectSensitiveWordsByType("书名"));
        List<SensitiveWords> authorSensitiveWords = safeList(sensitiveWordMapper.detectSensitiveWordsByType("作者"));

        List<ProblemBook> problemBooks = safeList(problemBookMapper.selectList(null));
        Map<String, ProblemBook> problemBookByIsbn = new LinkedHashMap<>();
        List<ProblemBookMatchCandidate> problemBookNameCandidates = problemBooks.stream()
                .map(problemBook -> ProblemBookMatchCandidate.builder()
                        .problemBook(problemBook)
                        .normalizedBookName(normalize(problemBook.getBookName()))
                        .build())
                .filter(candidate -> StringUtils.hasText(candidate.getNormalizedBookName()))
                .peek(candidate -> candidate.setNormalizedBookNameLength(candidate.getNormalizedBookName().length()))
                .collect(Collectors.toList());
        for (ProblemBook problemBook : problemBooks) {
            String isbn = normalize(problemBook.getIsbn());
            if (StringUtils.hasText(isbn) && !problemBookByIsbn.containsKey(isbn)) {
                problemBookByIsbn.put(isbn, problemBook);
            }
        }

        LambdaQueryWrapper<PublisherWhitelist> whitelistQuery = new LambdaQueryWrapper<>();
        whitelistQuery.eq(PublisherWhitelist::getIsActive, true);
        List<PublisherWhitelist> whitelist = safeList(publisherWhitelistMapper.selectList(whitelistQuery));
        Set<String> activeWhitelistPublishers = whitelist.stream()
                .map(PublisherWhitelist::getPublisherName)
                .map(this::normalize)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return DetectionReferenceDataSnapshot.builder()
                .keywordSensitiveWords(keywordSensitiveWords)
                .bookNameSensitiveWords(bookNameSensitiveWords)
                .authorSensitiveWords(authorSensitiveWords)
                .problemBookByIsbn(problemBookByIsbn)
                .problemBooks(problemBooks)
                .problemBookNameCandidates(problemBookNameCandidates)
                .activeWhitelistPublishers(activeWhitelistPublishers)
                .build();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
