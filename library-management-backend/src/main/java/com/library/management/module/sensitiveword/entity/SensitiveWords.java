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
     * 分类ID（必填）
     *
     * 说明：
     * - 数据库字段：category_id BIGINT NOT NULL
     * - 关联 sensitive_categories 表
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 敏感词内容（必填）
     *
     * 说明：
     * - 数据库字段：keyword VARCHAR(100) NOT NULL
     * - 敏感词的具体内容
     * - 长度限制：1-100 个字符
     */
    @TableField("keyword")
    private String keyword;

    /**
     * 匹配类型（必填）
     *
     * 说明：
     * - 数据库字段：match_type SMALLINT NOT NULL DEFAULT 1
     * - 0: 精确匹配
     * - 1: 模糊匹配
     * - 2: 正则表达式匹配
     */
    @TableField("match_type")
    private Integer matchType;

    /**
     * 风险等级（必填）
     *
     * 说明：
     * - 数据库字段：risk_level SMALLINT NOT NULL DEFAULT 2
     * - 1: 低风险
     * - 2: 中风险
     * - 3: 高风险
     */
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 检测类型（必填）
     *
     * 说明：
     * - 数据库字段：detection_type VARCHAR(20) NOT NULL DEFAULT '关键词'
     * - 关键词: 在书名、作者、内容简介等全部字段中检测
     * - 书名: 仅在书名（题名）字段中检测
     * - 作者: 仅在作者（著者1、著者2）字段中检测
     */
    @TableField("detection_type")
    private String detectionType;

    /**
     * 警报信息（可选）
     *
     * 说明：
     * - 数据库字段：alert_message VARCHAR(200)
     * - 命中该敏感词时显示的提示信息
     * - 用于提供更详细的风险说明
     */
    @TableField("alert_message")
    private String alertMessage;

    /**
     * 是否启用（必填）
     *
     * 说明：
     * - 数据库字段：is_active BOOLEAN DEFAULT TRUE
     * - true: 启用
     * - false: 禁用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 敏感词类别（临时字段，用于向后兼容）
     *
     * 说明：
     * - 此字段用于兼容旧代码
     * - 新代码应使用 category_id 字段
     */
    @TableField(exist = false)
    private String category;

    /**
     * 创建人（必填）
     *
     * 说明：
     * - 数据库字段：created_by BIGINT NOT NULL
     * - 记录创建该敏感词的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("created_by")
    private Long createdBy;

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
     * - 数据库字段：updated_by BIGINT
     * - 记录最后修改该敏感词的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("updated_by")
    private Long updatedBy;

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

























