package com.library.management.module.log.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.result.Result;
import com.library.management.module.log.dto.OperationLogDTO;
import com.library.management.module.log.dto.OperationLogExcelDTO;
import com.library.management.module.log.dto.OperationLogQueryRequest;
import com.library.management.module.log.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志控制器
 * 
 * 提供操作日志的查询和导出接口
 * 
 * 权限说明：
 * - 管理员可以查看所有日志
 * - 普通用户只能查看自己的操作日志（通过前端控制或查询条件过滤）
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Tag(name = "操作日志", description = "操作日志查询与导出接口")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     * 
     * @param request 查询条件（支持分页、多条件组合查询）
     * @return 分页结果
     */
    @Operation(summary = "查询操作日志列表", description = "支持分页和多条件组合查询")
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<Page<OperationLogDTO>> queryLogs(OperationLogQueryRequest request) {
        Page<OperationLogDTO> page = operationLogService.queryLogs(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询日志详情
     * 
     * @param logId 日志ID
     * @return 日志详情
     */
    @Operation(summary = "查询日志详情", description = "根据日志ID获取详细信息")
    @GetMapping("/{logId}")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<OperationLogDTO> getLogById(@PathVariable Long logId) {
        OperationLogDTO log = operationLogService.getLogById(logId);
        return Result.success(log);
    }

    /**
     * 查询指定记录的操作历史
     * 
     * 使用场景：查看某条数据的所有变更记录
     * 
     * @param module   模块名称
     * @param targetId 目标记录ID
     * @return 操作历史列表
     */
    @Operation(summary = "查询记录操作历史", description = "查询指定记录的所有操作历史")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<OperationLogDTO>> getLogsByTarget(
            @RequestParam String module,
            @RequestParam Long targetId) {
        List<OperationLogDTO> logs = operationLogService.getLogsByTarget(module, targetId);
        return Result.success(logs);
    }

    /**
     * 获取所有模块列表
     * 
     * 用于前端下拉选择框
     * 
     * @return 模块列表（包含英文标识和中文名称）
     */
    @Operation(summary = "获取模块列表", description = "获取所有可选的操作模块")
    @GetMapping("/modules")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<Map<String, String>>> getAllModules() {
        List<String> modules = operationLogService.getAllModules();
        List<Map<String, String>> result = modules.stream()
                .map(m -> Map.of(
                        "value", m,
                        "label", OperationLogDTO.getModuleName(m)))
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 获取所有操作类型列表
     * 
     * 用于前端下拉选择框
     * 
     * @return 操作类型列表（包含英文标识和中文名称）
     */
    @Operation(summary = "获取操作类型列表", description = "获取所有可选的操作类型")
    @GetMapping("/operation-types")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<Map<String, String>>> getAllOperationTypes() {
        List<String> types = operationLogService.getAllOperationTypes();
        List<Map<String, String>> result = types.stream()
                .map(t -> Map.of(
                        "value", t,
                        "label", OperationLogDTO.getOperationTypeName(t)))
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 导出操作日志到 Excel
     * 
     * @param request  查询条件
     * @param response HTTP 响应对象
     */
    @Operation(summary = "导出操作日志", description = "导出操作日志到 Excel 文件")
    @GetMapping("/export")
    @PreAuthorize("hasRole('admin')")
    public void exportLogs(OperationLogQueryRequest request, HttpServletResponse response) throws IOException {
        // 1. 查询数据
        List<OperationLogDTO> logs = operationLogService.exportLogs(request);

        // 2. 转换为 Excel DTO
        List<OperationLogExcelDTO> excelData = logs.stream()
                .map(OperationLogExcelDTO::fromDTO)
                .collect(Collectors.toList());

        // 3. 设置响应头
        String fileName = "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        // 4. 写入 Excel
        EasyExcel.write(response.getOutputStream(), OperationLogExcelDTO.class)
                .sheet("操作日志")
                .doWrite(excelData);
    }
}
