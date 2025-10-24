package com.library.management.module.problembook.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问题书目 Excel 导入/导出 DTO
 *
 * 用于 EasyExcel 导入导出功能
 * @ExcelProperty 注解指定 Excel 表头名称和列顺序
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemBookExcelDTO {

    /**
     * 书名（必填）
     * index = 0 表示第一列
     */
    @ExcelProperty(value = "书名", index = 0)
    private String bookName;

    /**
     * 作者
     * index = 1 表示第二列
     */
    @ExcelProperty(value = "作者", index = 1)
    private String author;

    /**
     * ISBN 编号
     * index = 2 表示第三列
     */
    @ExcelProperty(value = "ISBN", index = 2)
    private String isbn;

    /**
     * 出版社
     * index = 3 表示第四列
     */
    @ExcelProperty(value = "出版社", index = 3)
    private String publisher;

    /**
     * 出版年份
     * index = 4 表示第五列
     */
    @ExcelProperty(value = "出版年份", index = 4)
    private String publishYear;

    /**
     * 问题类型
     * index = 5 表示第六列
     */
    @ExcelProperty(value = "问题类型", index = 5)
    private String problemType;

    /**
     * 来源
     * index = 6 表示第七列
     */
    @ExcelProperty(value = "来源", index = 6)
    private String source;
}
