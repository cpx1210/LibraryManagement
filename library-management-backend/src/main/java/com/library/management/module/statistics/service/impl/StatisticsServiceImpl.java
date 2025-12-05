package com.library.management.module.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.management.module.collectionbook.mapper.CollectionBookMapper;
import com.library.management.module.detection.entity.BooklistCheckTask;
import com.library.management.module.detection.mapper.BooklistCheckTaskMapper;
import com.library.management.module.log.entity.OperationLog;
import com.library.management.module.log.mapper.OperationLogMapper;
import com.library.management.module.problembook.mapper.ProblemBookMapper;
import com.library.management.module.publisher.mapper.PublisherWhitelistMapper;
import com.library.management.module.sensitiveword.mapper.SensitiveWordMapper;
import com.library.management.module.statistics.dto.DashboardStatsDTO;
import com.library.management.module.statistics.service.StatisticsService;
import com.library.management.module.user.entity.SysUser;
import com.library.management.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计服务实现类
 * 
 * @author Library Management System
 * @since 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final CollectionBookMapper collectionBookMapper;
    private final ProblemBookMapper problemBookMapper;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final PublisherWhitelistMapper publisherWhitelistMapper;
    private final BooklistCheckTaskMapper booklistCheckTaskMapper;
    private final UserMapper userMapper;
    private final OperationLogMapper operationLogMapper;

    @Override
    public DashboardStatsDTO getOverviewStats() {
        // 馆藏图书总数
        Long collectionBookCount = collectionBookMapper.selectCount(null);

        // 问题书目数量
        Long problemBookCount = problemBookMapper.selectCount(null);

        // 敏感词数量
        Long sensitiveWordCount = sensitiveWordMapper.selectCount(null);

        // 出版社白名单数量
        Long publisherWhitelistCount = publisherWhitelistMapper.selectCount(null);

        // 检测任务总数
        Long detectionTaskCount = booklistCheckTaskMapper.selectCount(null);

        // 系统用户数量
        Long userCount = userMapper.selectCount(null);

        // 今日检测任务数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<BooklistCheckTask> todayTaskWrapper = new LambdaQueryWrapper<>();
        todayTaskWrapper.ge(BooklistCheckTask::getCreatedTime, todayStart);
        Long todayDetectionCount = booklistCheckTaskMapper.selectCount(todayTaskWrapper);

        // 今日发现问题数（检测任务中命中敏感词或问题书目的数量）
        LambdaQueryWrapper<BooklistCheckTask> todayProblemWrapper = new LambdaQueryWrapper<>();
        todayProblemWrapper.ge(BooklistCheckTask::getCreatedTime, todayStart)
                .and(w -> w.gt(BooklistCheckTask::getSensitiveHits, 0)
                        .or()
                        .gt(BooklistCheckTask::getProblemBookHits, 0));
        Long todayProblemCount = booklistCheckTaskMapper.selectCount(todayProblemWrapper);

        // 待处理问题数（状态为 pending 的任务）
        LambdaQueryWrapper<BooklistCheckTask> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(BooklistCheckTask::getStatus, "pending");
        Long pendingProblemCount = booklistCheckTaskMapper.selectCount(pendingWrapper);

        return DashboardStatsDTO.builder()
                .collectionBookCount(collectionBookCount)
                .problemBookCount(problemBookCount)
                .sensitiveWordCount(sensitiveWordCount)
                .publisherWhitelistCount(publisherWhitelistCount)
                .detectionTaskCount(detectionTaskCount)
                .userCount(userCount)
                .todayDetectionCount(todayDetectionCount)
                .todayProblemCount(todayProblemCount)
                .pendingProblemCount(pendingProblemCount)
                .build();
    }

    @Override
    public List<Map<String, Object>> getSensitiveWordDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 按检测类型统计敏感词数量
        // 关键词
        Long keywordCount = sensitiveWordMapper.countByDetectionType("关键词");
        result.add(createDistributionItem("关键词", keywordCount != null ? keywordCount : 0L));

        // 书名
        Long bookNameCount = sensitiveWordMapper.countByDetectionType("书名");
        result.add(createDistributionItem("书名", bookNameCount != null ? bookNameCount : 0L));

        // 作者
        Long authorCount = sensitiveWordMapper.countByDetectionType("作者");
        result.add(createDistributionItem("作者", authorCount != null ? authorCount : 0L));

        return result;
    }

    @Override
    public List<Map<String, Object>> getDetectionTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 获取近7天的数据
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            // 统计当天的检测任务数
            LambdaQueryWrapper<BooklistCheckTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(BooklistCheckTask::getCreatedTime, startOfDay)
                    .lt(BooklistCheckTask::getCreatedTime, endOfDay);
            Long count = booklistCheckTaskMapper.selectCount(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(formatter));
            item.put("count", count);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentDetections() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询最近5条检测任务
        LambdaQueryWrapper<BooklistCheckTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BooklistCheckTask::getCreatedTime)
                .last("LIMIT 5");

        List<BooklistCheckTask> tasks = booklistCheckTaskMapper.selectList(wrapper);

        for (BooklistCheckTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getTaskId());
            item.put("taskName", task.getTaskName());
            item.put("status", task.getStatus());
            item.put("statusText", getStatusText(task.getStatus()));
            item.put("totalBooks", task.getTotalBooks());
            item.put("sensitiveHits", task.getSensitiveHits());
            item.put("problemBookHits", task.getProblemBookHits());
            item.put("createdTime", task.getCreatedTime());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentLogs() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询最近5条操作日志
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getOperationTime)
                .last("LIMIT 5");

        List<OperationLog> logs = operationLogMapper.selectList(wrapper);

        // 获取操作人信息
        Set<Long> userIds = new HashSet<>();
        for (OperationLog log : logs) {
            if (log.getOperatedBy() != null) {
                userIds.add(log.getOperatedBy());
            }
        }

        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = userMapper.selectBatchIds(userIds);
            for (SysUser user : users) {
                userNameMap.put(user.getUserId(), user.getRealName());
            }
        }

        for (OperationLog log : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("logId", log.getLogId());
            item.put("module", log.getModule());
            item.put("moduleName", getModuleName(log.getModule()));
            item.put("operationType", log.getOperationType());
            item.put("operationTypeName", getOperationTypeName(log.getOperationType()));
            item.put("operatorName", userNameMap.getOrDefault(log.getOperatedBy(), "未知"));
            item.put("operationTime", log.getOperationTime());
            result.add(item);
        }

        return result;
    }

    // ========== 辅助方法 ==========

    private Map<String, Object> createDistributionItem(String name, Long value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private String getStatusText(String status) {
        if (status == null)
            return "未知";
        return switch (status) {
            case "pending" -> "待处理";
            case "processing" -> "处理中";
            case "success" -> "已完成";
            case "failed" -> "失败";
            case "cancelled" -> "已取消";
            default -> status;
        };
    }

    private String getModuleName(String module) {
        if (module == null)
            return "";
        return switch (module) {
            case "sensitive_word" -> "敏感词库";
            case "problem_book" -> "问题书目";
            case "publisher_whitelist" -> "出版社白名单";
            case "user" -> "用户管理";
            case "detection" -> "书单检测";
            case "collection_book" -> "馆藏图书";
            default -> module;
        };
    }

    private String getOperationTypeName(String operationType) {
        if (operationType == null)
            return "";
        return switch (operationType) {
            case "create" -> "新增";
            case "update" -> "修改";
            case "delete" -> "删除";
            case "import" -> "导入";
            case "export" -> "导出";
            default -> operationType;
        };
    }
}
