package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书单上传/检测任务创建响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BooklistUploadResponse {

    private Long taskId;

    private String taskName;

    private String status;

    private Integer totalBooks;

    private Integer processedBooks;

    private Integer currentBatch;

    private Integer totalBatches;

    private String message;
}
