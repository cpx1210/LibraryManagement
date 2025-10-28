package com.library.management.module.purchased.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 已购问题图书库实体类
 *
 * 对应数据库表：purchased_problem_books
 *
 * 功能说明：
 * 1. 存储已购买的问题图书信息
 * 2. 记录图书的处理状态和处理人
 * 3. 支持按状态、ISBN等查询
 *
 * @author Library Management System
 * @since 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("purchased_problem_books")
public class PurchasedProblemBook {

    /**
     * 图书资产号（主键，自增）
     *
     * 说明：
     * - 数据库字段：asset_code BIGSERIAL PRIMARY KEY
     * - 主键自增，插入数据时不需要手动设置
     */
    @TableId(value = "asset_code", type = IdType.AUTO)
    private Long assetCode;

    /**
     * 系统号
     *
     * 说明：
     * - 数据库字段：system_id VARCHAR(20)
     * - 可选值：system_1（aleph系统）、system_2（超星系统）
     */
    @TableField("system_id")
    private String systemId;

    /**
     * ISBN号（可选）
     */
    @TableField("isbn")
    private String isbn;

    /**
     * 书名（必填）
     *
     * 说明：
     * - 数据库字段：book_name VARCHAR(500) NOT NULL
     */
    @TableField("book_name")
    private String bookName;

    /**
     * 作者（可选）
     */
    @TableField("author")
    private String author;

    /**
     * 出版社（可选）
     */
    @TableField("publisher")
    private String publisher;

    /**
     * 购入时间（可选）
     *
     * 说明：
     * - 数据库字段：purchase_date DATE
     * - 图书采购日期
     */
    @TableField("purchase_date")
    private LocalDate purchaseDate;

    /**
     * 库存位置（可选）
     *
     * 说明：
     * - 数据库字段：location VARCHAR(100)
     * - 图书在馆内的存放位置
     */
    @TableField("location")
    private String location;

    /**
     * 问题原因（可选）
     *
     * 说明：
     * - 数据库字段：problem_reason TEXT
     * - 记录图书存在的问题及原因
     */
    @TableField("problem_reason")
    private String problemReason;

    /**
     * 处理状态（默认：pending）
     *
     * 说明：
     * - 数据库字段：status VARCHAR(20) DEFAULT 'pending'
     * - 可选值：
     *   - pending：待处理
     *   - processing：处理中
     *   - processed：已处理
     *   - reinstated：已恢复
     */
    @TableField("status")
    private String status;

    /**
     * 处理人ID（可选）
     *
     * 说明：
     * - 数据库字段：handler_id BIGINT
     * - 记录处理该问题图书的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理时间（可选）
     *
     * 说明：
     * - 数据库字段：handler_date TIMESTAMP
     * - 记录图书处理的时间
     */
    @TableField("handler_date")
    private LocalDateTime handlerDate;
}
