package com.library.management.module.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard 统计数据 DTO
 * 
 * @author Library Management System
 * @since 2025-12-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * 馆藏图书总数
     */
    private Long collectionBookCount;

    /**
     * 问题书目数量
     */
    private Long problemBookCount;

    /**
     * 敏感词数量
     */
    private Long sensitiveWordCount;

    /**
     * 出版社白名单数量
     */
    private Long publisherWhitelistCount;

    /**
     * 检测任务总数
     */
    private Long detectionTaskCount;

    /**
     * 系统用户数量
     */
    private Long userCount;

    /**
     * 今日检测任务数
     */
    private Long todayDetectionCount;

    /**
     * 今日发现问题数
     */
    private Long todayProblemCount;

    /**
     * 待处理问题数
     */
    private Long pendingProblemCount;
}
