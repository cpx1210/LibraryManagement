package com.library.management.module.sensitiveword.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 敏感词修改请求
 * 用于更新敏感词信息
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
public class SensitiveWordUpdateRequest {

    /**
     * 敏感词ID（必填，用于指定要修改的敏感词）
     */
    @NotNull(message = "敏感词ID不能为空")
    private Long wordId;

    /**
     * 敏感词内容（可选，如果修改会检查唯一性）
     */
    @Size(min = 1, max = 100, message = "敏感词内容长度必须为1-100个字符")
    private String keyword;

    /**
     * 敏感词分类ID（可选）
     * 关联 sensitive_categories 表
     */
    private Long categoryId;

    /**
     * 检测类型（可选）
     * 关键词: 在书名、作者、内容简介等全部字段中检测
     * 书名: 仅在书名（题名）字段中检测
     * 作者: 仅在作者（著者）字段中检测
     */
    private String detectionType;

    /**
     * 警报信息（可选）
     * 命中该敏感词时显示的提示信息
     */
    @Size(max = 200, message = "警报信息长度不能超过200个字符")
    private String alertMessage;

    /**
     * 匹配类型（可选）
     * 0: 精确匹配
     * 1: 模糊匹配
     * 2: 正则表达式匹配
     */
    private Integer matchType;

    /**
     * 风险等级（可选）
     * 1: 低风险
     * 2: 中风险
     * 3: 高风险
     */
    private Integer riskLevel;

    /**
     * 是否启用（可选）
     */
    private Boolean isActive;
}
