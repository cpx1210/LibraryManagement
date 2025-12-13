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
 * 列顺序：余码、题名、著者、ISBN、出版社、出版年、分馆、索书号、单价、批次、是否入库、馆藏位置
 *
 * @author Library Management System
 * @since 2025-12-05
 * @updated 2025-12-13 - 调整列顺序
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionBookExcelDTO {

    /**
     * 条码（余码）
     * index = 0 表示第一列
     */
    @ExcelProperty(value = "余码", index = 0)
    @ColumnWidth(20)
    private String barcode;

    /**
     * 题名
     * index = 1 表示第二列
     */
    @ExcelProperty(value = "题名", index = 1)
    @ColumnWidth(40)
    private String bookName;

    /**
     * 著者
     * index = 2 表示第三列
     */
    @ExcelProperty(value = "著者", index = 2)
    @ColumnWidth(20)
    private String author;

    /**
     * ISBN
     * index = 3 表示第四列
     */
    @ExcelProperty(value = "ISBN", index = 3)
    @ColumnWidth(18)
    private String isbn;

    /**
     * 出版社
     * index = 4 表示第五列
     */
    @ExcelProperty(value = "出版社", index = 4)
    @ColumnWidth(25)
    private String publisher;

    /**
     * 出版年
     * index = 5 表示第六列
     */
    @ExcelProperty(value = "出版年", index = 5)
    @ColumnWidth(10)
    private String publishYear;

    /**
     * 分馆
     * index = 6 表示第七列
     */
    @ExcelProperty(value = "分馆", index = 6)
    @ColumnWidth(15)
    private String branchLibrary;

    /**
     * 索书号
     * index = 7 表示第八列
     */
    @ExcelProperty(value = "索书号", index = 7)
    @ColumnWidth(15)
    private String callNumber;

    /**
     * 单价
     * index = 8 表示第九列
     */
    @ExcelProperty(value = "单价", index = 8)
    @ColumnWidth(10)
    private BigDecimal price;

    /**
     * 批次
     * index = 9 表示第十列
     */
    @ExcelProperty(value = "批次", index = 9)
    @ColumnWidth(12)
    private String batch;

    /**
     * 是否入库（是/否）
     * index = 10 表示第十一列
     */
    @ExcelProperty(value = "是否入库", index = 10)
    @ColumnWidth(10)
    private String isStoredStr;

    /**
     * 馆藏位置（合并馆藏院舍和书架位置）
     * index = 11 表示第十二列
     */
    @ExcelProperty(value = "馆藏位置", index = 11)
    @ColumnWidth(25)
    private String libraryLocation;

    /**
     * 书架位置（已合并到馆藏位置，此字段保留用于兼容）
     * index = 12 表示第十三列
     */
    @ExcelProperty(value = "书架位置", index = 12)
    @ColumnWidth(15)
    private String shelfLocation;

    /**
     * 重复标记（是/否）
     * index = 13 表示第十四列
     */
    @ExcelProperty(value = "重复标记", index = 13)
    @ColumnWidth(10)
    private String duplicateFlagStr;

    /**
     * 是否问题图书（是/否）
     * 用于导入时区分正常馆藏和问题图书
     * index = 14 表示第十五列
     */
    @ExcelProperty(value = "是否问题图书", index = 14)
    @ColumnWidth(12)
    private String isProblemStr;

    /**
     * 问题类型
     * index = 15 表示第十六列
     */
    @ExcelProperty(value = "问题类型", index = 15)
    @ColumnWidth(15)
    private String problemType;

    /**
     * 问题原因/备注
     * index = 16 表示第十七列
     */
    @ExcelProperty(value = "问题原因/备注", index = 16)
    @ColumnWidth(30)
    private String problemReason;
}
