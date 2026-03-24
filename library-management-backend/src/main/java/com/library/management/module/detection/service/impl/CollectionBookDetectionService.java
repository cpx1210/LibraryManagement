package com.library.management.module.detection.service.impl;

import com.library.management.common.exception.BusinessException;
import com.library.management.module.collectionbook.mapper.CollectionBookMapper;
import com.library.management.module.detection.dto.BooklistUploadResponse;
import com.library.management.module.detection.dto.CollectionBookCheckRequest;
import com.library.management.module.detection.entity.BooklistCheckTask;
import com.library.management.module.detection.mapper.BooklistCheckTaskMapper;
import jakarta.annotation.Resource;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 馆藏检测任务创建服务
 */
@Slf4j
@Service
public class CollectionBookDetectionService {

    private static final int DETECTION_BATCH_SIZE = 10_000;
    private static final String TASK_REJECTED_MESSAGE = "检测任务排队已满，请稍后重试";

    @Resource
    private CollectionBookMapper collectionBookMapper;

    @Resource
    private BooklistCheckTaskMapper taskMapper;

    @Resource
    private BooklistCheckAsyncService asyncService;

    @Transactional(rollbackFor = Exception.class)
    public BooklistUploadResponse createTask(CollectionBookCheckRequest request, Long userId, String userName) {
        CollectionBookCheckRequest safeRequest = request == null ? new CollectionBookCheckRequest() : request;
        log.info("用户 {} 从馆藏创建检测任务", userName);

        long totalBooks = countBooksForTask(safeRequest);
        if (totalBooks <= 0) {
            throw new BusinessException("未找到符合条件的馆藏书目");
        }

        String taskName = safeRequest.getTaskName();
        if (!StringUtils.hasText(taskName)) {
            taskName = "馆藏书目检测" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        }

        BooklistCheckTask task = BooklistCheckTask.builder()
                .taskName(taskName)
                .taskType("馆藏检测")
                .submittedBy(userId)
                .submitTime(LocalDateTime.now())
                .originalFilename("馆藏书目数据")
                .status("pending")
                .totalBooks(Math.toIntExact(totalBooks))
                .processedBooks(0)
                .currentBatch(0)
                .totalBatches(calculateTotalBatches(totalBooks))
                .sensitiveHits(0)
                .problemBookHits(0)
                .nonWhitelistPubs(0)
                .totalProblemBooks(0)
                .createdTime(LocalDateTime.now())
                .build();

        taskMapper.insert(task);
        triggerAsyncAfterCommit(task.getTaskId(), safeRequest);

        return BooklistUploadResponse.builder()
                .taskId(task.getTaskId())
                .taskName(taskName)
                .status("pending")
                .totalBooks(task.getTotalBooks())
                .processedBooks(0)
                .currentBatch(0)
                .totalBatches(task.getTotalBatches())
                .message("检测任务创建成功，已拆分为 " + task.getTotalBatches() + " 批执行")
                .build();
    }

    private long countBooksForTask(CollectionBookCheckRequest request) {
        if (isOnlyProblemFilter(request)) {
            return request.getIsProblem() != null && request.getIsProblem() == 1
                    ? collectionBookMapper.countProblemBooks()
                    : collectionBookMapper.countNormalBooks();
        }

        if (isOnlyProblemAndBranchFilter(request)) {
            return collectionBookMapper.countByProblemAndBranch(request.getIsProblem(), request.getBranchLibrary());
        }

        return collectionBookMapper.countCollectionBooksByConditions(request);
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

    private int calculateTotalBatches(long totalBooks) {
        return Math.max(1, (int) Math.ceil(totalBooks / (double) DETECTION_BATCH_SIZE));
    }

    private void triggerAsyncAfterCommit(Long taskId, CollectionBookCheckRequest request) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    submitCollectionTask(taskId, request);
                }
            });
            return;
        }
        submitCollectionTask(taskId, request);
    }

    private void submitCollectionTask(Long taskId, CollectionBookCheckRequest request) {
        try {
            asyncService.processCollectionTask(taskId, request);
        } catch (RejectedExecutionException ex) {
            log.warn("馆藏检测任务提交被拒绝：taskId={}, message={}", taskId, ex.getMessage());
            markTaskAsFailed(taskId, TASK_REJECTED_MESSAGE);
        }
    }

    private void markTaskAsFailed(Long taskId, String errorMessage) {
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        task.setStatus("failed");
        task.setErrorMessage(errorMessage);
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }
}
