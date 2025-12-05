package com.library.management.module.collectionbook.dto;

import lombok.Data;

/**
 * 馆藏图书查询请求 DTO
 * 支持多条件分页查询
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
public class CollectionBookQueryRequest {

    /**
     * 条码（精确查询）
     */
    private String barcode;

    /**
     * 题名（模糊查询）
     */
    private String bookName;

    /**
     * 著者（模糊查询）
     */
    private String author;

    /**
     * ISBN（精确查询）
     */
    private String isbn;

    /**
     * 出版社（模糊查询）
     */
    private String publisher;

    /**
     * 出版年（精确查询）
     */
    private String publishYear;

    /**
     * 分馆（精确查询）
     */
    private String branchLibrary;

    /**
     * 索书号（模糊查询）
     */
    private String callNumber;

    /**
     * 批次（精确查询）
     */
    private String batch;

    /**
     * 是否入库（精确查询）
     * 0-否，1-是
     */
    private Integer isStored;

    /**
     * 馆藏院舍（模糊查询）
     */
    private String libraryLocation;

    /**
     * 重复标记（精确查询）
     * 0-否，1-是
     */
    private Integer duplicateFlag;

    /**
     * 是否问题图书（精确查询）
     * 0-正常馆藏，1-问题图书
     * 用于区分"馆藏图书"和"馆藏问题图书"列表
     */
    private Integer isProblem;

    /**
     * 问题类型（模糊查询）
     */
    private String problemType;

    /**
     * 页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;
}
