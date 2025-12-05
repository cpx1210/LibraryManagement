package com.library.management.module.collectionbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 馆藏图书实体类
 *
 * 对应数据库表：collection_books
 *
 * 功能说明：
 * 1. 存储图书馆馆藏图书信息
 * 2. 通过 is_problem 字段区分正常馆藏和问题图书
 * 3. 支持按条码、ISBN、书名、作者等多维度查询
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection_books")
public class CollectionBook {

    /**
     * 条码（主键）
     *
     * 说明：
     * - 数据库字段：barcode VARCHAR(50) PRIMARY KEY
     * - 图书的唯一标识，从外部系统导入
     * - 非自增，需要手动设置
     */
    @TableId(value = "barcode", type = IdType.INPUT)
    private String barcode;

    /**
     * 题名（书名，必填）
     *
     * 说明：
     * - 数据库字段：book_name VARCHAR(500) NOT NULL
     */
    @TableField("book_name")
    private String bookName;

    /**
     * 著者（作者）
     *
     * 说明：
     * - 数据库字段：author VARCHAR(200)
     * - 可以为多个作者
     */
    @TableField("author")
    private String author;

    /**
     * ISBN 编号
     *
     * 说明：
     * - 数据库字段：isbn VARCHAR(20)
     * - 支持 ISBN-10 和 ISBN-13 格式
     */
    @TableField("isbn")
    private String isbn;

    /**
     * 出版社
     *
     * 说明：
     * - 数据库字段：publisher VARCHAR(200)
     */
    @TableField("publisher")
    private String publisher;

    /**
     * 出版年
     *
     * 说明：
     * - 数据库字段：publish_year VARCHAR(10)
     * - 格式：YYYY 或其他灵活格式
     */
    @TableField("publish_year")
    private String publishYear;

    /**
     * 分馆
     *
     * 说明：
     * - 数据库字段：branch_library VARCHAR(100)
     * - 图书所属的分馆名称
     */
    @TableField("branch_library")
    private String branchLibrary;

    /**
     * 索书号
     *
     * 说明：
     * - 数据库字段：call_number VARCHAR(100)
     * - 图书分类索引号
     */
    @TableField("call_number")
    private String callNumber;

    /**
     * 单价
     *
     * 说明：
     * - 数据库字段：price DECIMAL(10, 2)
     * - 图书定价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 批次
     *
     * 说明：
     * - 数据库字段：batch VARCHAR(50)
     * - 采购批次标识
     */
    @TableField("batch")
    private String batch;

    /**
     * 是否入库
     *
     * 说明：
     * - 数据库字段：is_stored TINYINT(1) DEFAULT 1
     * - 0: 未入库
     * - 1: 已入库（默认）
     */
    @TableField("is_stored")
    private Integer isStored;

    /**
     * 馆藏院舍
     *
     * 说明：
     * - 数据库字段：library_location VARCHAR(100)
     * - 图书所在的院舍/建筑
     */
    @TableField("library_location")
    private String libraryLocation;

    /**
     * 书架位置
     *
     * 说明：
     * - 数据库字段：shelf_location VARCHAR(100)
     * - 图书在书架上的具体位置
     */
    @TableField("shelf_location")
    private String shelfLocation;

    /**
     * 重复标记
     *
     * 说明：
     * - 数据库字段：duplicate_flag TINYINT(1) DEFAULT 0
     * - 0: 非重复
     * - 1: 重复
     */
    @TableField("duplicate_flag")
    private Integer duplicateFlag;

    /**
     * 是否问题图书
     *
     * 说明：
     * - 数据库字段：is_problem TINYINT(1) DEFAULT 0
     * - 0: 正常馆藏
     * - 1: 问题图书
     * - 用于区分"馆藏图书"和"馆藏问题图书"
     */
    @TableField("is_problem")
    private Integer isProblem;

    /**
     * 问题类型
     *
     * 说明：
     * - 数据库字段：problem_type VARCHAR(100)
     * - 当 is_problem = 1 时，记录问题分类
     */
    @TableField("problem_type")
    private String problemType;

    /**
     * 问题原因/备注
     *
     * 说明：
     * - 数据库字段：problem_reason TEXT
     * - 当 is_problem = 1 时，记录问题详细原因
     */
    @TableField("problem_reason")
    private String problemReason;

    /**
     * 创建人用户ID
     *
     * 说明：
     * - 数据库字段：created_by BIGINT
     * - 外键关联 sys_user.user_id
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     *
     * 说明：
     * - 数据库字段：create_time DATETIME DEFAULT CURRENT_TIMESTAMP
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人用户ID
     *
     * 说明：
     * - 数据库字段：updated_by BIGINT
     * - 外键关联 sys_user.user_id
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新时间
     *
     * 说明：
     * - 数据库字段：update_time DATETIME ON UPDATE CURRENT_TIMESTAMP
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
