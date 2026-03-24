package com.library.management.module.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 书单检测任务实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("booklist_check_task")
public class BooklistCheckTask {

    @TableId(type = IdType.AUTO)
    private Long taskId;

    private String taskName;

    private String taskType;

    private Long submittedBy;

    private LocalDateTime submitTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String originalFilename;

    private String filePath;

    private String resultFilePath;

    /**
     * pending / processing / success / failed / cancelled
     */
    private String status;

    private Integer totalBooks;

    private Integer processedBooks;

    private Integer currentBatch;

    private Integer totalBatches;

    private Integer sensitiveHits;

    private Integer problemBookHits;

    private Integer nonWhitelistPubs;

    private Integer totalProblemBooks;

    private String errorMessage;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
}
