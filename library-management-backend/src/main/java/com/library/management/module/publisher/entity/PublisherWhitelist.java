package com.library.management.module.publisher.entity;

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
 * 出版社白名单实体类
 *
 * 对应数据库表：publisher_whitelist
 *
 * 功能说明：
 * 1. 存储白名单出版社信息（正规、可信赖的出版社）
 * 2. 用于书单检测时验证出版社是否在白名单内
 * 3. 支持按出版社名称查询和匹配
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
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("publisher_whitelist")
public class PublisherWhitelist {

    /**
     * 出版社ID（主键，自增）
     *
     * 说明：
     * - 数据库字段：publisher_id BIGSERIAL PRIMARY KEY
     * - 主键自增，插入数据时不需要手动设置
     * - 类型：Long（对应数据库的 BIGINT）
     */
    @TableId(value = "publisher_id", type = IdType.AUTO)
    private Long publisherId;

    /**
     * 出版社名称（必填，唯一）
     *
     * 说明：
     * - 数据库字段：publisher_name VARCHAR(200) UNIQUE NOT NULL
     * - 出版社的全称
     * - 长度限制：1-200 个字符
     * - 唯一约束：同一出版社名称不能重复
     * - 用于精确匹配检测
     */
    @TableField("publisher_name")
    private String publisherName;

    /**
     * 年份批次（可选）
     *
     * 说明：
     * - 数据库字段：years BIGINT
     * - 用于区分不同年度的百大出版社名单
     * - 格式：YYYY（如：2023）
     * - 注意：此字段业务逻辑待明确，目前仅作为数据保存字段
     */
    @TableField("years")
    private Long years;

    /**
     * 是否启用（默认启用）
     *
     * 说明：
     * - 数据库字段：is_active BOOLEAN DEFAULT TRUE
     * - true：启用，参与书单检测
     * - false：禁用，不参与书单检测
     * - 默认值：true
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 创建人（必填）
     *
     * 说明：
     * - 数据库字段：created_by BIGINT NOT NULL
     * - 记录创建该白名单记录的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间（自动生成）
     *
     * 说明：
     * - 数据库字段：create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * - 记录白名单创建时间
     * - 插入数据时数据库自动设置当前时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
