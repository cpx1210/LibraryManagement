package com.library.management.module.publisher.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 出版社白名单 Excel 导入/导出 DTO
 *
 * 用于 EasyExcel 导入导出功能
 * @ExcelProperty 注解指定 Excel 表头名称和列顺序
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherWhitelistExcelDTO {

    /**
     * 出版社名称（必填）
     * index = 0 表示第一列
     */
    @ExcelProperty(value = "出版社名称", index = 0)
    private String publisherName;

    /**
     * 年份批次
     * index = 1 表示第二列
     * 注意：业务逻辑待明确
     */
    @ExcelProperty(value = "年份批次", index = 1)
    private String years;

    /**
     * 是否启用
     * index = 2 表示第三列
     * 值：启用/禁用
     */
    @ExcelProperty(value = "是否启用", index = 2)
    private String isActive;
}
