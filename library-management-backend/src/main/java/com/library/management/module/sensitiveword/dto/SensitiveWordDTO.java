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
     * 敏感词分类ID
     */
    private Long categoryId;

    /**
     * 敏感词分类名称（用于前端显示）
     */
    private String categoryName;

    /**
     * 匹配类型
     * 0: 精确匹配, 1: 模糊匹配, 2: 正则表达式匹配
     */
    private Integer matchType;

    /**
     * 风险等级
     * 1: 低风险, 2: 中风险, 3: 高风险
     */
    private Integer riskLevel;

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

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
