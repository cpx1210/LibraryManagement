package com.library.management.module.problembook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问题书目 DTO（数据传输对象）
 * 用于返回给前端
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemBookDTO {

    /**
     * 书目ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * ISBN 编号
     */
    private String isbn;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版年份
     */
    private String publishYear;

    /**
     * 问题类型
     */
    private String problemType;

    /**
     * 来源
     */
    private String source;

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
