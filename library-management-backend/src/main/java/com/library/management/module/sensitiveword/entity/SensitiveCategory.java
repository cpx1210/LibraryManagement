package com.library.management.module.sensitiveword.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 敏感词分类实体类
 *
 * 对应数据库表：sensitive_categories
 *
 * 功能说明：
 * 1. 存储敏感词分类信息
 * 2. 支持敏感词按类别管理（作者、书名、出版社、关键词等）
 * 3. 关联敏感词表，用于敏感词分类
 *
 * @author Library Management System
 * @since 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sensitive_categories")
public class SensitiveCategory {

    /**
     * 分类ID（主键，自增）
     */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /**
     * 分类名称（必填，唯一）
     *
     * 说明：
     * - 数据库字段：category_name VARCHAR(50) UNIQUE NOT NULL
     * - 分类名称，如：作者、书名、出版社、关键词
     * - 唯一约束，不能重复
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类描述（可选）
     *
     * 说明：
     * - 数据库字段：description VARCHAR(200)
     * - 分类的详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建人（必填）
     *
     * 说明：
     * - 数据库字段：created_by BIGINT NOT NULL
     * - 记录创建该分类的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间（自动生成）
     *
     * 说明：
     * - 数据库字段：create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * - 记录分类创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（可选）
     *
     * 说明：
     * - 数据库字段：updated_by BIGINT
     * - 记录最后修改该分类的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新时间（可选）
     *
     * 说明：
     * - 数据库字段：update_time TIMESTAMP
     * - 记录分类最后修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
