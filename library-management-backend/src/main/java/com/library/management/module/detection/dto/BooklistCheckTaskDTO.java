package com.library.management.module.detection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 书单检测任务响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BooklistCheckTaskDTO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 提交人ID
     */
    private Long submittedBy;

    /**
     * 提交人姓名
     */
    private String submitterName;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务状态文本
     */
    private String statusText;

    /**
     * 总书目数
     */
    private Integer totalBooks;

    /**
     * 命中敏感词数量
     */
    private Integer sensitiveHits;

    /**
     * 命中问题书目数量
     */
    private Integer problemBookHits;

    /**
     * 非白名单出版社数量
     */
    private Integer nonWhitelistPubs;

    /**
     * 总问题书目数
     */
    private Integer totalProblemBooks;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 耗时（秒）
     */
    private Long durationSeconds;
}