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
 * 对应数据库表：booklist_check_task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("booklist_check_task")
public class BooklistCheckTask {

    /**
     * 任务ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long taskId;

    /**
     * 任务名称
     * 格式：提交人_日期_次数（例如：张三_20251026_01）
     */
    private String taskName;

    /**
     * 任务类型
     * 例如：批量检测、单书检测
     */
    private String taskType;

    /**
     * 提交人ID
     */
    private Long submittedBy;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 结果文件存储路径
     */
    private String resultFilePath;

    /**
     * 任务状态
     * pending - 待处理
     * processing - 处理中
     * success - 成功
     * failed - 失败
     * cancelled - 已取消
     */
    private String status;

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
     * 总问题书目数（sensitiveHits + problemBookHits）
     */
    private Integer totalProblemBooks;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
