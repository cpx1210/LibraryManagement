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

    private Long taskId;

    private String taskName;

    private String taskType;

    private Long submittedBy;

    private String submitterName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String originalFilename;

    private String status;

    private String statusText;

    private Integer totalBooks;

    private Integer processedBooks;

    private Integer currentBatch;

    private Integer totalBatches;

    private Integer progressPercent;

    private Integer sensitiveHits;

    private Integer problemBookHits;

    private Integer nonWhitelistPubs;

    private Integer totalProblemBooks;

    private String errorMessage;

    private Long durationSeconds;

    private String exportStatus;

    private String exportStatusText;

    private Integer exportProgressPercent;

    private String exportErrorMessage;

    private Boolean exportFileReady;
}
