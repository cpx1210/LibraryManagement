package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敏感词命中详情 DTO
 * 用于记录具体命中的字段、关键词和原因
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveHitDetailDTO {

    /**
     * 命中的字段名称
     * 如：书名、著者、著者1、著者2、副题名、出版社、内容简介等
     */
    private String fieldName;

    /**
     * 命中的关键词
     */
    private String keyword;

    /**
     * 原因/警告信息
     * 来自敏感词的 alert_message 字段
     */
    private String reason;

    /**
     * 检测类型
     * 如：关键词、书名、作者
     */
    private String detectionType;

    /**
     * 风险等级
     * 1-低，2-中，3-高
     */
    private Integer riskLevel;

    /**
     * 生成显示文本
     * 格式：字段名匹配到关键词：xxx（原因：yyy）
     */
    public String toDisplayText() {
        StringBuilder sb = new StringBuilder();
        sb.append(fieldName).append("匹配到关键词：").append(keyword);
        if (reason != null && !reason.isEmpty()) {
            sb.append("（原因：").append(reason).append("）");
        }
        return sb.toString();
    }
}
