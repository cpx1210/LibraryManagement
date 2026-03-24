package com.library.management.module.detection.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检测结果 Excel 导出 DTO。
 *
 * 使用 EasyExcel 流式写出，避免整份工作簿常驻内存。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckResultExcelDTO {

    @ExcelProperty("书号")
    @ColumnWidth(18)
    private String bookNumber;

    @ExcelProperty("ISBN")
    @ColumnWidth(20)
    private String isbn;

    @ExcelProperty("题名")
    @ColumnWidth(28)
    private String bookName;

    @ExcelProperty("副题名")
    @ColumnWidth(24)
    private String subtitle;

    @ExcelProperty("著者1")
    @ColumnWidth(20)
    private String author1;

    @ExcelProperty("著者2")
    @ColumnWidth(20)
    private String author2;

    @ExcelProperty("出版地")
    @ColumnWidth(18)
    private String publishLocation;

    @ExcelProperty("出版社")
    @ColumnWidth(22)
    private String publisher;

    @ExcelProperty("出版日期")
    @ColumnWidth(16)
    private String publishDate;

    @ExcelProperty("读者对象")
    @ColumnWidth(18)
    private String targetAudience;

    @ExcelProperty("内容简介")
    @ColumnWidth(40)
    private String contentSummary;

    @ExcelProperty("分类号")
    @ColumnWidth(16)
    private String classificationNumber;

    @ExcelProperty("作品语种")
    @ColumnWidth(14)
    private String language;

    @ExcelProperty("风险等级")
    @ColumnWidth(12)
    private String riskLevelText;

    @ExcelProperty("命中敏感词")
    @ColumnWidth(12)
    private String hitSensitiveText;

    @ExcelProperty("命中问题书目")
    @ColumnWidth(14)
    private String hitProblemBookText;

    @ExcelProperty("白名单出版社")
    @ColumnWidth(14)
    private String whitelistPublisherText;

    @ExcelProperty("备注")
    @ColumnWidth(50)
    private String remark;
}
