package com.library.management.module.detection.dto;

import lombok.Data;

/**
 * 检测任务查询请求
 */
@Data
public class TaskQueryRequest {

    /**
     * 任务名称（模糊查询）
     */
    private String taskName;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 提交人ID
     */
    private Long submittedBy;

    /**
     * 当前页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;
}
