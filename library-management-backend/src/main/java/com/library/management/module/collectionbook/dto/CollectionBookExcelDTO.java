package com.library.management.module.collectionbook.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 馆藏图书 Excel 导入导出 DTO
 * 用于 EasyExcel 读写 Excel 文件
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionBookExcelDTO {

    /**
     * 条码
     */
    @ExcelProperty("条码")
    @ColumnWidth(20)
    private String barcode;

    /**
     * 题名
     */
    @ExcelProperty("题名")
    @ColumnWidth(40)
    private String bookName;

    /**
     * 著者
     */
    @ExcelProperty("著者")
    @ColumnWidth(20)
    private String author;

    /**
     * ISBN
     */
    @ExcelProperty("ISBN")
    @ColumnWidth(18)
    private String isbn;

    /**
     * 出版社
     */
    @ExcelProperty("出版社")
    @ColumnWidth(25)
    private String publisher;

    /**
     * 出版年
     */
    @ExcelProperty("出版年")
    @ColumnWidth(10)
    private String publishYear;

    /**
     * 分馆
     */
    @ExcelProperty("分馆")
    @ColumnWidth(15)
    private String branchLibrary;

    /**
     * 索书号
     */
    @ExcelProperty("索书号")
    @ColumnWidth(15)
    private String callNumber;

    /**
     * 单价
     */
    @ExcelProperty("单价")
    @ColumnWidth(10)
    private BigDecimal price;

    /**
     * 批次
     */
    @ExcelProperty("批次")
    @ColumnWidth(12)
    private String batch;

    /**
     * 是否入库（是/否）
     */
    @ExcelProperty("是否入库")
    @ColumnWidth(10)
    private String isStoredStr;

    /**
     * 馆藏院舍
     */
    @ExcelProperty("馆藏院舍")
    @ColumnWidth(15)
    private String libraryLocation;

    /**
     * 书架位置
     */
    @ExcelProperty("书架位置")
    @ColumnWidth(15)
    private String shelfLocation;

    /**
     * 重复标记（是/否）
     */
    @ExcelProperty("重复标记")
    @ColumnWidth(10)
    private String duplicateFlagStr;

    /**
     * 是否问题图书（是/否）
     * 用于导入时区分正常馆藏和问题图书
     */
    @ExcelProperty("是否问题图书")
    @ColumnWidth(12)
    private String isProblemStr;

    /**
     * 问题类型
     */
    @ExcelProperty("问题类型")
    @ColumnWidth(15)
    private String problemType;

    /**
     * 问题原因/备注
     */
    @ExcelProperty("问题原因/备注")
    @ColumnWidth(30)
    private String problemReason;
}
