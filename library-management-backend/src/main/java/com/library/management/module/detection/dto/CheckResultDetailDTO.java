package com.library.management.module.detection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检测结果明细响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckResultDetailDTO {

    /**
     * 明细ID
     */
    private Long detailId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 是否命中敏感词
     */
    private Boolean hitSensitive;

    /**
     * 是否命中问题书目
     */
    private Boolean hitProblemBook;

    /**
     * 是否为白名单出版社
     */
    private Boolean isWhitelistPublisher;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险等级文本
     */
    private String riskLevelText;

    /**
     * 命中的敏感词（简单格式，用于兼容）
     */
    private String sensitiveWords;

    /**
     * 命中的敏感词详细信息列表
     * 包含：字段名、关键词、原因等详细信息
     */
    private List<SensitiveHitDetailDTO> sensitiveHitDetails;

    /**
     * 检测时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectionTime;

    /**
     * 检测状态
     */
    private String checkStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 备注信息
     */
    private String remark;
}