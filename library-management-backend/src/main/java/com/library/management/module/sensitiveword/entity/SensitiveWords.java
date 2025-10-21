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
 * 敏感词实体类
 *
 * 对应数据库表：sensitive_words
 *
 * 功能说明：
 * 1. 存储敏感词汇信息
 * 2. 用于图书信息、评论等内容的敏感词过滤
 * 3. 支持按类别分类管理
 *
 * 注解说明：
 * - @TableName：MyBatis-Plus 注解，指定数据库表名
 * - @TableId：MyBatis-Plus 注解，标记主键字段，type = IdType.AUTO 表示主键自增
 * - @TableField：MyBatis-Plus 注解，映射数据库字段名
 * - @Data：Lombok 注解，自动生成 getter/setter、toString、equals、hashCode 方法
 * - @Builder：Lombok 注解，支持建造者模式创建对象
 * - @NoArgsConstructor：Lombok 注解，生成无参构造函数
 * - @AllArgsConstructor：Lombok 注解，生成全参构造函数
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sensitive_words")
public class SensitiveWords {

    /**
     * 敏感词ID（主键，自增）
     *
     * 说明：
     * - 数据库字段：word_id BIGSERIAL PRIMARY KEY
     * - 主键自增，插入数据时不需要手动设置
     * - 类型：Long（对应数据库的 BIGINT）
     */
    @TableId(value = "word_id", type = IdType.AUTO)
    private Long wordId;

    /**
     * 敏感词内容（必填，唯一）
     *
     * 说明：
     * - 数据库字段：keyword VARCHAR(100) UNIQUE NOT NULL
     * - 敏感词的具体内容
     * - 长度限制：1-100 个字符
     */
    @TableField("keyword")
    private String keyword;

    /**
     * 敏感词类别（必填）
     *
     * 说明：
     * - 数据库字段：category VARCHAR(50) NOT NULL
     * - 敏感词分类，如：政治、色情、暴力、违禁等
     */
    @TableField("category")
    private String category;

    /**
     * 创建人（必填）
     *
     * 说明：
     * - 数据库字段：created_by VARCHAR(50) NOT NULL
     * - 记录创建该敏感词的用户名
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间（自动生成）
     *
     * 说明：
     * - 数据库字段：create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * - 记录敏感词创建时间
     * - 插入数据时数据库自动设置当前时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（可选）
     *
     * 说明：
     * - 数据库字段：updated_by VARCHAR(50)
     * - 记录最后修改该敏感词的用户名
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 更新时间（可选）
     *
     * 说明：
     * - 数据库字段：update_time TIMESTAMP
     * - 记录敏感词最后修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}

























