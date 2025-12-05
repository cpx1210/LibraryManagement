package com.library.management.module.collectionbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 馆藏图书创建请求 DTO
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
public class CollectionBookCreateRequest {

    /**
     * 条码（主键，必填）
     */
    @NotBlank(message = "条码不能为空")
    @Size(max = 50, message = "条码长度不能超过50个字符")
    private String barcode;

    /**
     * 题名（必填）
     */
    @NotBlank(message = "题名不能为空")
    @Size(max = 500, message = "题名长度不能超过500个字符")
    private String bookName;

    /**
     * 著者
     */
    @Size(max = 200, message = "著者长度不能超过200个字符")
    private String author;

    /**
     * ISBN
     */
    @Size(max = 20, message = "ISBN长度不能超过20个字符")
    private String isbn;

    /**
     * 出版社
     */
    @Size(max = 200, message = "出版社长度不能超过200个字符")
    private String publisher;

    /**
     * 出版年
     */
    @Size(max = 10, message = "出版年长度不能超过10个字符")
    private String publishYear;

    /**
     * 分馆
     */
    @Size(max = 100, message = "分馆长度不能超过100个字符")
    private String branchLibrary;

    /**
     * 索书号
     */
    @Size(max = 100, message = "索书号长度不能超过100个字符")
    private String callNumber;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 批次
     */
    @Size(max = 50, message = "批次长度不能超过50个字符")
    private String batch;

    /**
     * 是否入库（默认1-已入库）
     */
    private Integer isStored = 1;

    /**
     * 馆藏院舍
     */
    @Size(max = 100, message = "馆藏院舍长度不能超过100个字符")
    private String libraryLocation;

    /**
     * 书架位置
     */
    @Size(max = 100, message = "书架位置长度不能超过100个字符")
    private String shelfLocation;

    /**
     * 重复标记（默认0-非重复）
     */
    private Integer duplicateFlag = 0;

    /**
     * 是否问题图书（默认0-正常馆藏）
     */
    private Integer isProblem = 0;

    /**
     * 问题类型（当 isProblem=1 时填写）
     */
    @Size(max = 100, message = "问题类型长度不能超过100个字符")
    private String problemType;

    /**
     * 问题原因/备注
     */
    private String problemReason;
}
