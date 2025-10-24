package com.library.management.module.problembook.dto;

import lombok.Data;

/**
 * 问题书目查询请求参数
 * 用于分页查询和多条件过滤
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Data
public class ProblemBookQueryRequest {

    /**
     * 页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 书名（模糊查询）
     */
    private String bookName;

    /**
     * 作者（模糊查询）
     */
    private String author;

    /**
     * ISBN 编号（精确查询）
     */
    private String isbn;

    /**
     * 出版社（模糊查询）
     */
    private String publisher;

    /**
     * 出版年份（精确查询）
     */
    private String publishYear;

    /**
     * 问题类型（模糊查询）
     */
    private String problemType;
}
