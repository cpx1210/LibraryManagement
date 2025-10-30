package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书单上传响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BooklistUploadResponse {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 上传的书目总数
     */
    private Integer totalBooks;

    /**
     * 提示信息
     */
    private String message;
}