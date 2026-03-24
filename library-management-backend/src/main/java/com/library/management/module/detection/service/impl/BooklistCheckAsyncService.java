package com.library.management.module.detection.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.module.collectionbook.entity.CollectionBook;
import com.library.management.module.collectionbook.mapper.CollectionBookMapper;
import com.library.management.module.detection.dto.BookItemDTO;
import com.library.management.module.detection.dto.CollectionBookCheckRequest;
import com.library.management.module.detection.dto.DetectionResultDTO;
import com.library.management.module.detection.entity.BooklistCheckDetail;
import com.library.management.module.detection.entity.BooklistCheckTask;
import com.library.management.module.detection.mapper.BooklistCheckDetailMapper;
import com.library.management.module.detection.mapper.BooklistCheckTaskMapper;
import com.library.management.module.detection.service.DetectionEngine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检测任务异步执行器
 */
@Slf4j
@Service
public class BooklistCheckAsyncService {

    private static final int COLLECTION_BATCH_SIZE = 10_000;
    private static final int PROGRESS_UPDATE_BATCH_SIZE = 200;
    private static final int DETAIL_INSERT_BATCH_SIZE = 500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private BooklistCheckTaskMapper taskMapper;

    @Resource
    private BooklistCheckDetailMapper detailMapper;

    @Resource
    private DetectionEngine detectionEngine;

    @Resource
    private CollectionBookMapper collectionBookMapper;

    @Async
    public void processUploadTask(Long taskId) {
        log.info("开始异步执行上传检测任务: taskId={}", taskId);
        try {
            BooklistCheckTask task = startTask(taskId);
            if (task == null) {
                return;
            }

            int totalBooks = safeInt(task.getTotalBooks());
            int totalBatches = getOrInitTotalBatches(task, totalBooks);

            int sensitiveHits = safeInt(task.getSensitiveHits());
            int problemBookHits = safeInt(task.getProblemBookHits());
            int nonWhitelistPubs = safeInt(task.getNonWhitelistPubs());
            int totalProblemBooks = safeInt(task.getTotalProblemBooks());
            int processedBooks = safeInt(task.getProcessedBooks());

            for (int batchIndex = safeInt(task.getCurrentBatch()); batchIndex < totalBatches; batchIndex++) {
                if (isCancelled(taskId)) {
                    finishCancelled(taskId);
                    return;
                }

                List<BooklistCheckDetail> details = detailMapper.selectTaskBatch(
                        taskId,
                        (long) batchIndex * COLLECTION_BATCH_SIZE,
                        COLLECTION_BATCH_SIZE);
                if (details.isEmpty()) {
                    break;
                }

                log.info("任务 {} 开始处理上传批次 {}/{}，本批 {} 条",
                        taskId, batchIndex + 1, totalBatches, details.size());

                for (int i = 0; i < details.size(); i += PROGRESS_UPDATE_BATCH_SIZE) {
                    int end = Math.min(i + PROGRESS_UPDATE_BATCH_SIZE, details.size());
                    List<BooklistCheckDetail> subDetails = details.subList(i, end);

                    List<BookItemDTO> books = subDetails.stream()
                            .map(this::convertDetailToBook)
                            .toList();
                    List<DetectionResultDTO> results = detectionEngine.batchDetect(books);

                    for (int j = 0; j < subDetails.size() && j < results.size(); j++) {
                        BooklistCheckDetail detail = subDetails.get(j);
                        DetectionResultDTO result = results.get(j);
                        applyDetectionResult(detail, result);
                        detailMapper.updateById(detail);

                        sensitiveHits += boolToInt(result.getHitSensitive());
                        problemBookHits += boolToInt(result.getHitProblemBook());
                        nonWhitelistPubs += Boolean.FALSE.equals(result.getIsWhitelistPublisher()) ? 1 : 0;
                        totalProblemBooks += result.isProblemBook() ? 1 : 0;
                    }

                    processedBooks += subDetails.size();
                    updateTaskProgress(taskId, processedBooks, batchIndex + 1, totalBatches,
                            sensitiveHits, problemBookHits, nonWhitelistPubs, totalProblemBooks);
                }

                log.info("任务 {} 完成上传批次 {}/{}，累计已处理 {} 条",
                        taskId, batchIndex + 1, totalBatches, processedBooks);
            }

            finishSuccess(taskId, totalBooks, totalBatches, sensitiveHits, problemBookHits, nonWhitelistPubs,
                    totalProblemBooks);
        } catch (Exception e) {
            handleFailure(taskId, e);
        }
    }

    @Async
    public void processCollectionTask(Long taskId, CollectionBookCheckRequest request) {
        log.info("开始异步执行馆藏检测任务: taskId={}", taskId);
        try {
            BooklistCheckTask task = startTask(taskId);
            if (task == null) {
                return;
            }

            int totalBooks = safeInt(task.getTotalBooks());
            int totalBatches = getOrInitTotalBatches(task, totalBooks);

            DetectionCounters counters = new DetectionCounters();

            if (supportsCursorScan(request)) {
                processCollectionTaskByCursor(taskId, request, totalBatches, counters);
            } else {
                processCollectionTaskByOffset(taskId, request, totalBatches, counters);
            }

            finishSuccess(taskId, totalBooks, totalBatches, counters.sensitiveHits, counters.problemBookHits,
                    counters.nonWhitelistPubs, counters.totalProblemBooks);
        } catch (Exception e) {
            handleFailure(taskId, e);
        }
    }

    private void processCollectionTaskByCursor(Long taskId,
                                               CollectionBookCheckRequest request,
                                               int totalBatches,
                                               DetectionCounters counters) {
        LocalDateTime lastCreateTime = null;
        String lastBarcode = null;
        int batchIndex = 0;

        while (true) {
            if (isCancelled(taskId)) {
                finishCancelled(taskId);
                return;
            }

            List<CollectionBook> batchBooks = fetchNextCollectionBatch(request, lastCreateTime, lastBarcode);
            if (batchBooks.isEmpty()) {
                break;
            }

            batchIndex++;
            log.info("任务 {} 开始处理馆藏批次 {}/{}，本批 {} 条",
                    taskId, batchIndex, totalBatches, batchBooks.size());

            processCollectionChunk(taskId, batchBooks, batchIndex, totalBatches, counters);

            CollectionBook lastBook = batchBooks.get(batchBooks.size() - 1);
            lastCreateTime = lastBook.getCreateTime();
            lastBarcode = lastBook.getBarcode();

            log.info("任务 {} 完成馆藏批次 {}/{}，累计已处理 {} 条",
                    taskId, batchIndex, totalBatches, counters.processedBooks);
        }
    }

    private void processCollectionTaskByOffset(Long taskId,
                                               CollectionBookCheckRequest request,
                                               int totalBatches,
                                               DetectionCounters counters) {
        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            if (isCancelled(taskId)) {
                finishCancelled(taskId);
                return;
            }

            List<CollectionBook> batchBooks = collectionBookMapper.selectCollectionBooksBatchByConditions(
                    request,
                    (long) batchIndex * COLLECTION_BATCH_SIZE,
                    COLLECTION_BATCH_SIZE);
            if (batchBooks.isEmpty()) {
                break;
            }

            log.info("任务 {} 开始处理馆藏批次 {}/{}，本批 {} 条",
                    taskId, batchIndex + 1, totalBatches, batchBooks.size());

            processCollectionChunk(taskId, batchBooks, batchIndex + 1, totalBatches, counters);

            log.info("任务 {} 完成馆藏批次 {}/{}，累计已处理 {} 条",
                    taskId, batchIndex + 1, totalBatches, counters.processedBooks);
        }
    }

    private void processCollectionChunk(Long taskId,
                                        List<CollectionBook> batchBooks,
                                        int currentBatch,
                                        int totalBatches,
                                        DetectionCounters counters) {
        for (int i = 0; i < batchBooks.size(); i += PROGRESS_UPDATE_BATCH_SIZE) {
            int end = Math.min(i + PROGRESS_UPDATE_BATCH_SIZE, batchBooks.size());
            List<CollectionBook> subBooks = batchBooks.subList(i, end);

            List<BookItemDTO> detectBooks = subBooks.stream()
                    .map(this::convertCollectionBookToBookItem)
                    .toList();
            List<DetectionResultDTO> results = detectionEngine.batchDetect(detectBooks);

            List<BooklistCheckDetail> details = new ArrayList<>(results.size());
            for (int j = 0; j < subBooks.size() && j < results.size(); j++) {
                CollectionBook collectionBook = subBooks.get(j);
                DetectionResultDTO result = results.get(j);
                BooklistCheckDetail detail = buildCollectionDetail(taskId, collectionBook, result);
                details.add(detail);

                counters.sensitiveHits += boolToInt(result.getHitSensitive());
                counters.problemBookHits += boolToInt(result.getHitProblemBook());
                counters.nonWhitelistPubs += Boolean.FALSE.equals(result.getIsWhitelistPublisher()) ? 1 : 0;
                counters.totalProblemBooks += result.isProblemBook() ? 1 : 0;
            }

            batchInsertDetails(details);
            counters.processedBooks += details.size();

            updateTaskProgress(taskId, counters.processedBooks, currentBatch, totalBatches,
                    counters.sensitiveHits, counters.problemBookHits, counters.nonWhitelistPubs,
                    counters.totalProblemBooks);
        }
    }

    private List<CollectionBook> fetchNextCollectionBatch(CollectionBookCheckRequest request,
                                                          LocalDateTime lastCreateTime,
                                                          String lastBarcode) {
        if (isOnlyProblemAndBranchFilter(request)) {
            return collectionBookMapper.selectNextBatchByProblemAndBranch(
                    request.getIsProblem(),
                    request.getBranchLibrary(),
                    lastCreateTime,
                    lastBarcode,
                    COLLECTION_BATCH_SIZE);
        }

        return collectionBookMapper.selectNextBatchByProblem(
                request.getIsProblem(),
                lastCreateTime,
                lastBarcode,
                COLLECTION_BATCH_SIZE);
    }

    private boolean supportsCursorScan(CollectionBookCheckRequest request) {
        return isOnlyProblemFilter(request) || isOnlyProblemAndBranchFilter(request);
    }

    private boolean isOnlyProblemFilter(CollectionBookCheckRequest request) {
        return request != null
                && request.getIsProblem() != null
                && !StringUtils.hasText(request.getBarcode())
                && !StringUtils.hasText(request.getBookName())
                && !StringUtils.hasText(request.getAuthor())
                && !StringUtils.hasText(request.getIsbn())
                && !StringUtils.hasText(request.getPublisher())
                && !StringUtils.hasText(request.getPublishYear())
                && !StringUtils.hasText(request.getBranchLibrary())
                && !StringUtils.hasText(request.getCallNumber())
                && !StringUtils.hasText(request.getBatch())
                && request.getIsStored() == null
                && !StringUtils.hasText(request.getLibraryLocation())
                && request.getDuplicateFlag() == null
                && !StringUtils.hasText(request.getProblemType());
    }

    private boolean isOnlyProblemAndBranchFilter(CollectionBookCheckRequest request) {
        return request != null
                && request.getIsProblem() != null
                && StringUtils.hasText(request.getBranchLibrary())
                && !StringUtils.hasText(request.getBarcode())
                && !StringUtils.hasText(request.getBookName())
                && !StringUtils.hasText(request.getAuthor())
                && !StringUtils.hasText(request.getIsbn())
                && !StringUtils.hasText(request.getPublisher())
                && !StringUtils.hasText(request.getPublishYear())
                && !StringUtils.hasText(request.getCallNumber())
                && !StringUtils.hasText(request.getBatch())
                && request.getIsStored() == null
                && !StringUtils.hasText(request.getLibraryLocation())
                && request.getDuplicateFlag() == null
                && !StringUtils.hasText(request.getProblemType());
    }

    private BooklistCheckTask startTask(Long taskId) {
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("检测任务不存在: taskId={}", taskId);
            return null;
        }
        if ("cancelled".equals(task.getStatus())) {
            log.info("检测任务已取消，跳过执行: taskId={}", taskId);
            return null;
        }
        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        }
        task.setStatus("processing");
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        return task;
    }

    private int getOrInitTotalBatches(BooklistCheckTask task, int totalBooks) {
        int totalBatches = safeInt(task.getTotalBatches());
        if (totalBatches > 0) {
            return totalBatches;
        }
        totalBatches = Math.max(1, (int) Math.ceil(totalBooks / (double) COLLECTION_BATCH_SIZE));
        task.setTotalBatches(totalBatches);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        return totalBatches;
    }

    private boolean isCancelled(Long taskId) {
        BooklistCheckTask latest = taskMapper.selectById(taskId);
        return latest != null && "cancelled".equals(latest.getStatus());
    }

    private void finishCancelled(Long taskId) {
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        if (task.getEndTime() == null) {
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private void finishSuccess(Long taskId,
                               int totalBooks,
                               int totalBatches,
                               int sensitiveHits,
                               int problemBookHits,
                               int nonWhitelistPubs,
                               int totalProblemBooks) {
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null || "cancelled".equals(task.getStatus())) {
            return;
        }
        task.setStatus("success");
        task.setProcessedBooks(totalBooks);
        task.setCurrentBatch(totalBatches);
        task.setTotalBatches(totalBatches);
        task.setSensitiveHits(sensitiveHits);
        task.setProblemBookHits(problemBookHits);
        task.setNonWhitelistPubs(nonWhitelistPubs);
        task.setTotalProblemBooks(totalProblemBooks);
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void handleFailure(Long taskId, Exception e) {
        log.error("检测任务执行失败: taskId={}, message={}", taskId, e.getMessage(), e);
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null || "cancelled".equals(task.getStatus())) {
            return;
        }
        task.setStatus("failed");
        task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(e.getMessage());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void updateTaskProgress(Long taskId,
                                    int processedBooks,
                                    int currentBatch,
                                    int totalBatches,
                                    int sensitiveHits,
                                    int problemBookHits,
                                    int nonWhitelistPubs,
                                    int totalProblemBooks) {
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null || "cancelled".equals(task.getStatus())) {
            return;
        }
        task.setProcessedBooks(processedBooks);
        task.setCurrentBatch(currentBatch);
        task.setTotalBatches(totalBatches);
        task.setSensitiveHits(sensitiveHits);
        task.setProblemBookHits(problemBookHits);
        task.setNonWhitelistPubs(nonWhitelistPubs);
        task.setTotalProblemBooks(totalProblemBooks);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private BookItemDTO convertDetailToBook(BooklistCheckDetail detail) {
        return BookItemDTO.builder()
                .bookNumber(detail.getBookNumber())
                .bookName(detail.getBookName())
                .subtitle(detail.getSubtitle())
                .author1(detail.getAuthor1())
                .author2(detail.getAuthor2())
                .isbn(detail.getIsbn())
                .publishLocation(detail.getPublishLocation())
                .publisher(detail.getPublisher())
                .publishDate(detail.getPublishDate())
                .targetAudience(detail.getTargetAudience())
                .contentSummary(detail.getContentSummary())
                .classificationNumber(detail.getClassificationNumber())
                .language(detail.getLanguage())
                .build();
    }

    private BookItemDTO convertCollectionBookToBookItem(CollectionBook book) {
        return BookItemDTO.builder()
                .bookNumber(book.getBarcode())
                .bookName(book.getBookName())
                .author1(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishYear())
                .classificationNumber(book.getCallNumber())
                .build();
    }

    private BooklistCheckDetail buildCollectionDetail(Long taskId, CollectionBook book, DetectionResultDTO result) {
        BooklistCheckDetail detail = BooklistCheckDetail.builder()
                .taskId(taskId)
                .bookNumber(book.getBarcode())
                .isbn(book.getIsbn())
                .bookName(book.getBookName())
                .author1(book.getAuthor())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishYear())
                .classificationNumber(book.getCallNumber())
                .createdTime(LocalDateTime.now())
                .build();
        applyDetectionResult(detail, result);
        return detail;
    }

    private void applyDetectionResult(BooklistCheckDetail detail, DetectionResultDTO result) {
        detail.setHitSensitive(boolToInt(result.getHitSensitive()));
        detail.setHitProblemBook(boolToInt(result.getHitProblemBook()));
        detail.setIsWhitelistPublisher(Boolean.TRUE.equals(result.getIsWhitelistPublisher()) ? 1 : 0);
        detail.setRiskLevel(result.getRiskLevel());
        detail.setSensitiveWords(serializeSensitiveWords(result));
        detail.setDetectionTime(LocalDateTime.now());
        detail.setCheckStatus("completed");
        detail.setUpdatedTime(LocalDateTime.now());
    }

    private String serializeSensitiveWords(DetectionResultDTO result) {
        if (result.getSensitiveHitDetails() != null && !result.getSensitiveHitDetails().isEmpty()) {
            try {
                return objectMapper.writeValueAsString(result.getSensitiveHitDetails());
            } catch (Exception e) {
                log.warn("序列化敏感词详情失败，回退为简化格式: {}", e.getMessage());
            }
        }
        if (result.getSensitiveWords() != null && !result.getSensitiveWords().isEmpty()) {
            return String.join(",", result.getSensitiveWords());
        }
        return null;
    }

    private void batchInsertDetails(List<BooklistCheckDetail> details) {
        for (int i = 0; i < details.size(); i += DETAIL_INSERT_BATCH_SIZE) {
            int end = Math.min(i + DETAIL_INSERT_BATCH_SIZE, details.size());
            detailMapper.batchInsert(details.subList(i, end));
        }
    }

    private int boolToInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static class DetectionCounters {
        private int processedBooks;
        private int sensitiveHits;
        private int problemBookHits;
        private int nonWhitelistPubs;
        private int totalProblemBooks;
    }
}
