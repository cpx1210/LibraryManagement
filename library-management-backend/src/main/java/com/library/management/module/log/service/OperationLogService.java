package com.library.management.module.log.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.log.dto.OperationLogDTO;
import com.library.management.module.log.dto.OperationLogQueryRequest;
import com.library.management.module.log.entity.OperationLog;

import java.util.List;

/**
 * 操作日志服务接口
 * 
 * 功能说明：
 * 1. 日志查询（分页、条件查询）
 * 2. 日志保存（同步/异步）
 * 3. 日志导出
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
public interface OperationLogService {

    /**
     * 分页查询操作日志
     * 
     * @param request 查询条件
     * @return 分页结果
     */
    Page<OperationLogDTO> queryLogs(OperationLogQueryRequest request);

    /**
     * 根据ID查询日志详情
     * 
     * @param logId 日志ID
     * @return 日志详情
     */
    OperationLogDTO getLogById(Long logId);

    /**
     * 查询指定记录的操作历史
     * 
     * @param module   模块名称
     * @param targetId 目标记录ID
     * @return 操作历史列表
     */
    List<OperationLogDTO> getLogsByTarget(String module, Long targetId);

    /**
     * 保存操作日志（同步）
     * 
     * @param log 日志实体
     */
    void saveLog(OperationLog log);

    /**
     * 异步保存操作日志
     * 
     * 说明：使用 @Async 注解，不阻塞主业务
     * 
     * @param log 日志实体
     */
    void saveLogAsync(OperationLog log);

    /**
     * 保存操作日志（便捷方法）
     * 
     * @param module        操作模块
     * @param operationType 操作类型
     * @param targetId      目标记录ID
     * @param oldValue      操作前内容（JSON）
     * @param newValue      操作后内容（JSON）
     * @param operatedBy    操作人ID
     * @param ipAddress     IP地址
     */
    void saveLog(String module, String operationType, Long targetId,
            String oldValue, String newValue, Long operatedBy, String ipAddress);

    /**
     * 导出操作日志
     * 
     * @param request 查询条件
     * @return 日志列表（用于导出）
     */
    List<OperationLogDTO> exportLogs(OperationLogQueryRequest request);

    /**
     * 获取所有模块列表
     * 
     * @return 模块列表
     */
    List<String> getAllModules();

    /**
     * 获取所有操作类型列表
     * 
     * @return 操作类型列表
     */
    List<String> getAllOperationTypes();
}
