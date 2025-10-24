package com.library.management.module.publisher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 出版社白名单 DTO（数据传输对象）
 * 用于返回给前端
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherWhitelistDTO {

    /**
     * 出版社ID
     */
    private Long publisherId;

    /**
     * 出版社名称
     */
    private String publisherName;

    /**
     * 年份批次
     */
    private Long years;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
