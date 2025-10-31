package com.library.management.module.sensitiveword.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敏感词 Excel 导入/导出 DTO（更新版）
 *
 * 用于 EasyExcel 导入导出功能
 * @ExcelProperty 注解指定 Excel 表头名称和列顺序
 *
 * 新版模板列结构：
 * - 类型（关键词/书名/作者）
 * - 关键词
 * - 警报信息
 *
 * @author Library Management System
 * @since 2025-10-21
 * @updated 2025-10-31 - 适配新模板结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordExcelDTO {

    /**
     * 检测类型
     * 可选值：关键词、书名、作者
     * index = 0 表示第一列
     */
    @ExcelProperty(value = "类型", index = 0)
    private String detectionType;

    /**
     * 敏感词关键词
     * index = 1 表示第二列
     */
    @ExcelProperty(value = "关键词", index = 1)
    private String keyword;

    /**
     * 警报信息
     * 当命中该敏感词时显示的警告信息
     * index = 2 表示第三列
     */
    @ExcelProperty(value = "警报信息", index = 2)
    private String alertMessage;
}
