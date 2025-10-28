package com.library.management.module.sensitiveword.dto;

import lombok.Data;

/**
 * 敏感词查询请求参数
 * 用于分页查询和条件过滤
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
public class SensitiveWordQueryRequest {

    /**
     * 页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 敏感词内容（模糊查询）
     */
    private String keyword;

    /**
     * 敏感词分类ID（精确查询）
     */
    private Long categoryId;

    /**
     * 匹配类型（精确查询）
     * 0: 精确匹配, 1: 模糊匹配, 2: 正则表达式匹配
     */
    private Integer matchType;

    /**
     * 风险等级（精确查询）
     * 1: 低风险, 2: 中风险, 3: 高风险
     */
    private Integer riskLevel;

    /**
     * 是否启用（精确查询）
     */
    private Boolean isActive;
}
