package com.library.management.module.sensitiveword.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敏感词 Excel 导入/导出 DTO
 *
 * 用于 EasyExcel 导入导出功能
 * @ExcelProperty 注解指定 Excel 表头名称和列顺序
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordExcelDTO {

    /**
     * 敏感词内容
     * index = 0 表示第一列
     */
    @ExcelProperty(value = "敏感词内容", index = 0)
    private String keyword;

    /**
     * 敏感词类别
     * index = 1 表示第二列
     */
    @ExcelProperty(value = "敏感词类别", index = 1)
    private String category;
}
