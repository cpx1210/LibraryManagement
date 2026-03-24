package com.library.management.module.detection.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.module.collectionbook.entity.CollectionBook;
import com.library.management.module.collectionbook.mapper.CollectionBookMapper;
import com.library.management.module.detection.dto.BookItemDTO;
import com.library.management.module.detection.dto.CheckResultExcelDTO;
import com.library.management.module.detection.dto.CollectionBookCheckRequest;
import com.library.management.module.detection.dto.DetectionResultDTO;
import com.library.management.module.detection.dto.SensitiveHitDetailDTO;
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
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 检测任务异步执行器
 */
@Slf4j
@Service
public class BooklistCheckAsyncService {

    private static final int COLLECTION_BATCH_SIZE = 10_000;
    private static final int PROGRESS_UPDATE_BATCH_SIZE = 200;
    private static final int DETAIL_INSERT_BATCH_SIZE = 500;
    private static final int EXPORT_BATCH_SIZE = 5_000;
    private static final int EXPORT_MAX_ROWS_PER_SHEET = 1_000_000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private BooklistCheckTaskMapper taskMapper;

    @Resource
    private BooklistCheckDetailMapper detailMapper;

    @Resource
    private DetectionEngine detectionEngine;

    @Resource
    private CollectionBookMapper collectionBookMapper;

    @Resource
    private CheckResultExportStateService exportStateService;

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

    @Async
    public void processExportTask(Long taskId) {
        log.info("开始异步导出检测结果: taskId={}", taskId);

        Path tempFile = null;
        try {
            BooklistCheckTask task = taskMapper.selectById(taskId);
            if (task == null) {
                exportStateService.markFailed(taskId, "检测任务不存在");
                return;
            }
            if (!"success".equals(task.getStatus())) {
                exportStateService.markFailed(taskId, "检测尚未完成，暂时无法导出");
                return;
            }

            exportStateService.markProcessing(taskId, 0);

            Path targetFile = buildExportFilePath(task);
            tempFile = buildTempExportFilePath(targetFile);
            Files.createDirectories(targetFile.getParent());
            Files.deleteIfExists(tempFile);

            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                streamExportDetails(task, outputStream);
            }

            BooklistCheckTask latestTask = taskMapper.selectById(taskId);
            if (latestTask == null) {
                Files.deleteIfExists(tempFile);
                exportStateService.clear(taskId);
                log.info("导出任务已被删除，停止落盘导出文件: taskId={}", taskId);
                return;
            }

            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            latestTask.setResultFilePath(targetFile.toString());
            latestTask.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(latestTask);

            exportStateService.markSuccess(taskId);
            log.info("异步导出检测结果完成: taskId={}, path={}", taskId, targetFile);
        } catch (Exception e) {
            log.error("异步导出检测结果失败: taskId={}, message={}", taskId, e.getMessage(), e);
            exportStateService.markFailed(taskId, e.getMessage());
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ioException) {
                    log.warn("清理导出临时文件失败: taskId={}, message={}", taskId, ioException.getMessage());
                }
            }
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

    private void streamExportDetails(BooklistCheckTask task, OutputStream outputStream) throws IOException {
        ExcelWriter excelWriter = EasyExcel.write(outputStream, CheckResultExcelDTO.class)
                .autoCloseStream(false)
                .build();

        try {
            Long lastDetailId = 0L;
            int sheetNo = 0;
            int rowsInCurrentSheet = 0;
            long exportedRows = 0L;
            int totalBooks = Math.max(1, safeInt(task.getTotalBooks()));
            WriteSheet currentSheet = buildExportSheet(sheetNo);

            while (true) {
                List<BooklistCheckDetail> batch = detailMapper.selectExportBatchAfterDetailId(
                        task.getTaskId(),
                        lastDetailId,
                        EXPORT_BATCH_SIZE);
                if (batch.isEmpty()) {
                    break;
                }

                lastDetailId = batch.get(batch.size() - 1).getDetailId();
                List<CheckResultExcelDTO> exportRows = batch.stream()
                        .map(this::convertToExcelDTO)
                        .collect(Collectors.toList());

                int fromIndex = 0;
                while (fromIndex < exportRows.size()) {
                    if (rowsInCurrentSheet >= EXPORT_MAX_ROWS_PER_SHEET) {
                        sheetNo++;
                        rowsInCurrentSheet = 0;
                        currentSheet = buildExportSheet(sheetNo);
                    }

                    int writableCount = Math.min(
                            EXPORT_MAX_ROWS_PER_SHEET - rowsInCurrentSheet,
                            exportRows.size() - fromIndex);
                    List<CheckResultExcelDTO> currentRows = exportRows.subList(fromIndex, fromIndex + writableCount);
                    excelWriter.write(currentRows, currentSheet);

                    rowsInCurrentSheet += writableCount;
                    exportedRows += writableCount;
                    fromIndex += writableCount;
                }

                int progress = Math.min(99, (int) ((exportedRows * 100L) / totalBooks));
                exportStateService.markProcessing(task.getTaskId(), progress);
            }

            if (exportedRows == 0) {
                excelWriter.write(new ArrayList<CheckResultExcelDTO>(), currentSheet);
            }
        } finally {
            excelWriter.finish();
        }
    }

    private WriteSheet buildExportSheet(int sheetNo) {
        String sheetName = sheetNo == 0 ? "检测结果" : "检测结果-" + (sheetNo + 1);
        return EasyExcel.writerSheet(sheetNo, sheetName).build();
    }

    private CheckResultExcelDTO convertToExcelDTO(BooklistCheckDetail detail) {
        return CheckResultExcelDTO.builder()
                .bookNumber(detail.getBookNumber())
                .isbn(detail.getIsbn())
                .bookName(detail.getBookName())
                .subtitle(detail.getSubtitle())
                .author1(detail.getAuthor1())
                .author2(detail.getAuthor2())
                .publishLocation(detail.getPublishLocation())
                .publisher(detail.getPublisher())
                .publishDate(detail.getPublishDate())
                .targetAudience(detail.getTargetAudience())
                .contentSummary(detail.getContentSummary())
                .classificationNumber(detail.getClassificationNumber())
                .language(detail.getLanguage())
                .riskLevelText(getRiskLevelText(detail.getRiskLevel()))
                .hitSensitiveText(detail.getHitSensitive() == 1 ? "是" : "否")
                .hitProblemBookText(detail.getHitProblemBook() == 1 ? "是" : "否")
                .whitelistPublisherText(detail.getIsWhitelistPublisher() == 1 ? "是" : "否")
                .remark(buildExportRemark(detail))
                .build();
    }

    private String buildExportRemark(BooklistCheckDetail detail) {
        StringBuilder remark = new StringBuilder();

        if (detail.getHitSensitive() == 1) {
            remark.append("【敏感词】");
            String sensitiveWordsText = extractSensitiveWordsForDisplay(detail.getSensitiveWords());
            if (StringUtils.hasText(sensitiveWordsText)) {
                remark.append("命中敏感词：").append(sensitiveWordsText);
            }
        }

        if (detail.getHitProblemBook() == 1) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【问题书目】");
        }

        if (detail.getIsWhitelistPublisher() == 0) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【非白名单出版社】");
        }

        if (remark.length() == 0) {
            remark.append("无问题");
        }

        return remark.toString();
    }

    private String extractSensitiveWordsForDisplay(String sensitiveWordsRaw) {
        if (!StringUtils.hasText(sensitiveWordsRaw)) {
            return null;
        }

        if (!sensitiveWordsRaw.startsWith("[")) {
            return sensitiveWordsRaw;
        }

        try {
            List<SensitiveHitDetailDTO> hitDetails = objectMapper.readValue(
                    sensitiveWordsRaw,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SensitiveHitDetailDTO.class));
            return hitDetails.stream()
                    .map(SensitiveHitDetailDTO::getKeyword)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            log.warn("解析导出敏感词详情失败，使用原始内容：{}", e.getMessage());
            return sensitiveWordsRaw;
        }
    }

    private String getRiskLevelText(String riskLevel) {
        if (riskLevel == null) {
            return "";
        }

        return switch (riskLevel) {
            case "high" -> "高风险";
            case "medium" -> "中风险";
            case "low" -> "低风险";
            default -> riskLevel;
        };
    }

    private Path buildExportFilePath(BooklistCheckTask task) {
        String safeTaskName = task.getTaskName() == null ? "task-" + task.getTaskId() : task.getTaskName()
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        Path exportDir = Paths.get(System.getProperty("java.io.tmpdir"), "library-management", "exports");
        return exportDir.resolve("task-" + task.getTaskId() + "-" + safeTaskName + "_检测结果.xlsx");
    }

    private Path buildTempExportFilePath(Path targetFile) {
        return targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
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
