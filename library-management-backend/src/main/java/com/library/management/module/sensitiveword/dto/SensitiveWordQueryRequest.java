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
     * 敏感词类别（精确查询）
     */
    private String category;

    /**
     * 创建人（模糊查询）
     */
    private String createdBy;
}
