package com.library.management.module.statistics.service;

import com.library.management.module.statistics.dto.DashboardStatsDTO;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 * 
 * @author Library Management System
 * @since 2025-12-06
 */
public interface StatisticsService {

    /**
     * 获取 Dashboard 概览数据
     */
    DashboardStatsDTO getOverviewStats();

    /**
     * 获取敏感词分类分布
     */
    List<Map<String, Object>> getSensitiveWordDistribution();

    /**
     * 获取近7天检测任务趋势
     */
    List<Map<String, Object>> getDetectionTrend();

    /**
     * 获取最近检测任务
     */
    List<Map<String, Object>> getRecentDetections();

    /**
     * 获取最近操作日志
     */
    List<Map<String, Object>> getRecentLogs();
}
