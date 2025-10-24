package com.library.management.module.publisher.dto;

import lombok.Data;

/**
 * 出版社白名单查询请求参数
 * 用于分页查询和多条件过滤
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Data
public class PublisherWhitelistQueryRequest {

    /**
     * 页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 出版社名称（模糊查询）
     */
    private String publisherName;

    /**
     * 年份批次（精确查询）
     */
    private Long years;

    /**
     * 是否启用（精确查询）
     */
    private Boolean isActive;
}
