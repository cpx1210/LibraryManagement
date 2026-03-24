package com.library.management.module.detection.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 检测结果导出运行态管理。
 *
 * 导出任务只负责生成文件，不再阻塞前端请求。运行中状态保存在内存里，
 * 成功结果仍然通过任务表中的 result_file_path 持久化。
 */
@Service
public class CheckResultExportStateService {

    private final Map<Long, ExportRuntimeState> runtimeStates = new ConcurrentHashMap<>();

    public ExportStateSnapshot snapshot(Long taskId, String resultFilePath) {
        ExportRuntimeState runtimeState = runtimeStates.get(taskId);
        boolean fileReady = isFileReady(resultFilePath);

        if (runtimeState == null) {
            if (fileReady) {
                return ExportStateSnapshot.success();
            }
            return ExportStateSnapshot.idle();
        }

        if ("success".equals(runtimeState.getStatus()) && !fileReady) {
            runtimeStates.remove(taskId);
            return ExportStateSnapshot.idle();
        }

        if ("success".equals(runtimeState.getStatus()) && fileReady) {
            return ExportStateSnapshot.success();
        }

        return ExportStateSnapshot.builder()
                .status(runtimeState.getStatus())
                .statusText(getStatusText(runtimeState.getStatus()))
                .progressPercent(runtimeState.getProgressPercent())
                .errorMessage(runtimeState.getErrorMessage())
                .fileReady(fileReady)
                .build();
    }

    public void markPending(Long taskId) {
        runtimeStates.put(taskId, buildState("pending", 0, null));
    }

    public void markProcessing(Long taskId, int progressPercent) {
        runtimeStates.put(taskId, buildState("processing", progressPercent, null));
    }

    public void markSuccess(Long taskId) {
        runtimeStates.put(taskId, buildState("success", 100, null));
    }

    public void markFailed(Long taskId, String errorMessage) {
        runtimeStates.put(taskId, buildState("failed", 0, errorMessage));
    }

    public void clear(Long taskId) {
        runtimeStates.remove(taskId);
    }

    public boolean isRunning(Long taskId, String resultFilePath) {
        String status = snapshot(taskId, resultFilePath).getStatus();
        return "pending".equals(status) || "processing".equals(status);
    }

    private ExportRuntimeState buildState(String status, int progressPercent, String errorMessage) {
        return ExportRuntimeState.builder()
                .status(status)
                .progressPercent(progressPercent)
                .errorMessage(errorMessage)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private boolean isFileReady(String resultFilePath) {
        if (!StringUtils.hasText(resultFilePath)) {
            return false;
        }

        Path path = Paths.get(resultFilePath);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    private String getStatusText(String status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case "pending" -> "导出排队中";
            case "processing" -> "导出中";
            case "success" -> "导出完成";
            case "failed" -> "导出失败";
            default -> status;
        };
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExportRuntimeState {
        private String status;
        private int progressPercent;
        private String errorMessage;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportStateSnapshot {
        private String status;
        private String statusText;
        private int progressPercent;
        private String errorMessage;
        private boolean fileReady;

        public static ExportStateSnapshot idle() {
            return ExportStateSnapshot.builder()
                    .status("idle")
                    .statusText("未导出")
                    .progressPercent(0)
                    .fileReady(false)
                    .build();
        }

        public static ExportStateSnapshot success() {
            return ExportStateSnapshot.builder()
                    .status("success")
                    .statusText("导出完成")
                    .progressPercent(100)
                    .fileReady(true)
                    .build();
        }
    }
}
