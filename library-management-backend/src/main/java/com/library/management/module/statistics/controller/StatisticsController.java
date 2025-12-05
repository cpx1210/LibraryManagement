package com.library.management.module.statistics.controller;

import com.library.management.common.result.Result;
import com.library.management.module.statistics.dto.DashboardStatsDTO;
import com.library.management.module.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 * 
 * 提供 Dashboard 所需的各类统计数据
 * 
 * @author Library Management System
 * @since 2025-12-06
 */
@Tag(name = "统计数据", description = "Dashboard 统计数据接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取 Dashboard 概览数据
     */
    @Operation(summary = "获取概览统计", description = "获取首页所需的各项统计数据")
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<DashboardStatsDTO> getOverviewStats() {
        DashboardStatsDTO stats = statisticsService.getOverviewStats();
        return Result.success(stats);
    }

    /**
     * 获取敏感词分类分布
     */
    @Operation(summary = "敏感词分类分布", description = "获取敏感词按检测类型的分布数据")
    @GetMapping("/sensitive-word-distribution")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<Map<String, Object>>> getSensitiveWordDistribution() {
        List<Map<String, Object>> data = statisticsService.getSensitiveWordDistribution();
        return Result.success(data);
    }

    /**
     * 获取近7天检测任务趋势
     */
    @Operation(summary = "检测任务趋势", description = "获取近7天的检测任务数量趋势")
    @GetMapping("/detection-trend")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<Map<String, Object>>> getDetectionTrend() {
        List<Map<String, Object>> data = statisticsService.getDetectionTrend();
        return Result.success(data);
    }

    /**
     * 获取最近检测任务
     */
    @Operation(summary = "最近检测任务", description = "获取最近5条检测任务")
    @GetMapping("/recent-detections")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<Map<String, Object>>> getRecentDetections() {
        List<Map<String, Object>> data = statisticsService.getRecentDetections();
        return Result.success(data);
    }

    /**
     * 获取最近操作日志
     */
    @Operation(summary = "最近操作日志", description = "获取最近5条操作日志")
    @GetMapping("/recent-logs")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public Result<List<Map<String, Object>>> getRecentLogs() {
        List<Map<String, Object>> data = statisticsService.getRecentLogs();
        return Result.success(data);
    }
}
