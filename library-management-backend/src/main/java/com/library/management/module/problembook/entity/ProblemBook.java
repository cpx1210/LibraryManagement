package com.library.management.module.problembook.entity;

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
 * 问题书目实体类
 *
 * 对应数据库表：problem_books
 *
 * 功能说明：
 * 1. 存储问题图书信息（被列入黑名单的图书）
 * 2. 用于书单检测时匹配问题书目
 * 3. 支持按 ISBN、书名、作者、出版社等多维度查询
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
 * @since 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("problem_books")
public class ProblemBook {

    /**
     * 书目ID（主键，自增）
     *
     * 说明：
     * - 数据库字段：book_id BIGSERIAL PRIMARY KEY
     * - 主键自增，插入数据时不需要手动设置
     * - 类型：Long（对应数据库的 BIGINT）
     */
    @TableId(value = "book_id", type = IdType.AUTO)
    private Long bookId;

    /**
     * 书名（必填）
     *
     * 说明：
     * - 数据库字段：book_name VARCHAR(500) NOT NULL
     * - 问题图书的书名
     * - 长度限制：1-500 个字符
     * - 用于模糊匹配检测
     */
    @TableField("book_name")
    private String bookName;

    /**
     * 作者（可选）
     *
     * 说明：
     * - 数据库字段：author VARCHAR(200)
     * - 图书作者姓名
     * - 可以为多个作者（使用逗号分隔）
     */
    @TableField("author")
    private String author;

    /**
     * ISBN 编号（可选）
     *
     * 说明：
     * - 数据库字段：isbn VARCHAR(20)
     * - 支持 ISBN-10 和 ISBN-13 格式
     * - 用于精确匹配检测
     */
    @TableField("isbn")
    private String isbn;

    /**
     * 出版社（可选）
     *
     * 说明：
     * - 数据库字段：publisher VARCHAR(200)
     * - 图书出版社名称
     */
    @TableField("publisher")
    private String publisher;

    /**
     * 出版年份（可选）
     *
     * 说明：
     * - 数据库字段：publish_year VARCHAR(4)
     * - 图书出版年份
     * - 格式：YYYY（如：2023）
     */
    @TableField("publish_year")
    private String publishYear;

    /**
     * 问题类型（可选）
     *
     * 说明：
     * - 数据库字段：problem_type VARCHAR(100)
     * - 问题分类，如：政治问题、内容不当、盗版等
     */
    @TableField("problem_type")
    private String problemType;

    /**
     * 来源（可选）
     *
     * 说明：
     * - 数据库字段：source VARCHAR(500)
     * - 问题书目的来源说明
     * - 如：教育部通报、出版社通知、读者举报等
     */
    @TableField("source")
    private String source;

    /**
     * 创建人（必填）
     *
     * 说明：
     * - 数据库字段：created_by BIGINT NOT NULL
     * - 记录创建该问题书目的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间（自动生成）
     *
     * 说明：
     * - 数据库字段：create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * - 记录问题书目创建时间
     * - 插入数据时数据库自动设置当前时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人（可选）
     *
     * 说明：
     * - 数据库字段：updated_by BIGINT
     * - 记录最后修改该问题书目的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新时间（可选）
     *
     * 说明：
     * - 数据库字段：update_time TIMESTAMP
     * - 记录问题书目最后修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
