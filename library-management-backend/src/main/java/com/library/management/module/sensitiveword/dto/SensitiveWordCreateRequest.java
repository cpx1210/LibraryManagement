package com.library.management.module.sensitiveword.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 敏感词创建请求
 * 用于新增敏感词时接收前端提交的数据
 *
 * 使用 Bean Validation 注解进行参数校验
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
public class SensitiveWordCreateRequest {

    /**
     * 敏感词内容（必填，1-100字符）
     */
    @NotBlank(message = "敏感词内容不能为空")
    @Size(min = 1, max = 100, message = "敏感词内容长度必须为1-100个字符")
    private String keyword;

    /**
     * 敏感词分类ID（必填）
     * 关联 sensitive_categories 表
     */
    @NotNull(message = "敏感词分类ID不能为空")
    private Long categoryId;

    /**
     * 匹配类型（可选，默认为1-模糊匹配）
     * 0: 精确匹配
     * 1: 模糊匹配
     * 2: 正则表达式匹配
     */
    private Integer matchType = 1;

    /**
     * 风险等级（可选，默认为2-中风险）
     * 1: 低风险
     * 2: 中风险
     * 3: 高风险
     */
    private Integer riskLevel = 2;

    /**
     * 是否启用（可选，默认为true）
     */
    private Boolean isActive = true;
}
