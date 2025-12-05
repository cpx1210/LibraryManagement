package com.library.management.module.collectionbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 馆藏图书 DTO（数据传输对象）
 * 用于返回给前端
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionBookDTO {

    /**
     * 条码（主键）
     */
    private String barcode;

    /**
     * 题名（书名）
     */
    private String bookName;

    /**
     * 著者（作者）
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
     * 出版年
     */
    private String publishYear;

    /**
     * 分馆
     */
    private String branchLibrary;

    /**
     * 索书号
     */
    private String callNumber;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 批次
     */
    private String batch;

    /**
     * 是否入库（0-否，1-是）
     */
    private Integer isStored;

    /**
     * 馆藏院舍
     */
    private String libraryLocation;

    /**
     * 书架位置
     */
    private String shelfLocation;

    /**
     * 重复标记（0-否，1-是）
     */
    private Integer duplicateFlag;

    /**
     * 是否问题图书（0-正常馆藏，1-问题图书）
     */
    private Integer isProblem;

    /**
     * 问题类型
     */
    private String problemType;

    /**
     * 问题原因/备注
     */
    private String problemReason;

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
