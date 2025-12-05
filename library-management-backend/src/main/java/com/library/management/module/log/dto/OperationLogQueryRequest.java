package com.library.management.module.log.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询请求参数
 * 
 * 支持多条件组合查询：
 * - 按模块筛选
 * - 按操作类型筛选
 * - 按操作人筛选
 * - 按时间范围筛选
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
public class OperationLogQueryRequest {

    /**
     * 页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 操作模块（精确查询）
     * 
     * 可选值：
     * - sensitive_word：敏感词库
     * - problem_book：问题书目库
     * - publisher_whitelist：出版社白名单
     * - purchased_problem_book：已购问题图书
     * - user：用户管理
     * - detection：书单检测
     */
    private String module;

    /**
     * 操作类型（精确查询）
     * 
     * 可选值：
     * - create：新增
     * - update：修改
     * - delete：删除
     * - import：导入
     * - export：导出
     */
    private String operationType;

    /**
     * 操作人ID（精确查询）
     */
    private Long operatedBy;

    /**
     * 操作人姓名（模糊查询）
     */
    private String operatorName;

    /**
     * 开始时间（时间范围查询）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（时间范围查询）
     */
    private LocalDateTime endTime;

    /**
     * 目标记录ID（精确查询）
     * 用于查询某条记录的所有操作历史
     */
    private Long targetId;
}
