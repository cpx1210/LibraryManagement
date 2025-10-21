package com.library.management.module.sensitiveword.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 敏感词 DTO（数据传输对象）
 * 用于返回给前端
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordDTO {

    /**
     * 敏感词ID
     */
    private Long wordId;

    /**
     * 敏感词内容
     */
    private String keyword;

    /**
     * 敏感词类别
     */
    private String category;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
