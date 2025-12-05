package com.library.management.module.log.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * 操作日志 Excel 导出 DTO
 * 
 * 使用 EasyExcel 注解定义 Excel 列
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogExcelDTO {

    @ExcelProperty("日志ID")
    @ColumnWidth(15)
    private Long logId;

    @ExcelProperty("操作模块")
    @ColumnWidth(15)
    private String moduleName;

    @ExcelProperty("操作类型")
    @ColumnWidth(10)
    private String operationTypeName;

    @ExcelProperty("目标记录ID")
    @ColumnWidth(15)
    private Long targetId;

    @ExcelProperty("操作人")
    @ColumnWidth(15)
    private String operatorName;

    @ExcelProperty("操作时间")
    @ColumnWidth(20)
    private String operationTime;

    @ExcelProperty("IP地址")
    @ColumnWidth(15)
    private String ipAddress;

    @ExcelProperty("操作前内容")
    @ColumnWidth(50)
    private String oldValue;

    @ExcelProperty("操作后内容")
    @ColumnWidth(50)
    private String newValue;

    /**
     * 从 DTO 转换为 Excel DTO
     */
    public static OperationLogExcelDTO fromDTO(OperationLogDTO dto) {
        return OperationLogExcelDTO.builder()
                .logId(dto.getLogId())
                .moduleName(dto.getModuleName())
                .operationTypeName(dto.getOperationTypeName())
                .targetId(dto.getTargetId())
                .operatorName(dto.getOperatorName())
                .operationTime(dto.getOperationTime() != null
                        ? dto.getOperationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "")
                .ipAddress(dto.getIpAddress())
                .oldValue(dto.getOldValue())
                .newValue(dto.getNewValue())
                .build();
    }
}
