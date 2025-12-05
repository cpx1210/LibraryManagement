package com.library.management.module.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.exception.BusinessException;
import com.library.management.module.log.dto.OperationLogDTO;
import com.library.management.module.log.dto.OperationLogQueryRequest;
import com.library.management.module.log.entity.OperationLog;
import com.library.management.module.log.mapper.OperationLogMapper;
import com.library.management.module.log.service.OperationLogService;
import com.library.management.module.user.entity.SysUser;
import com.library.management.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 * 
 * 核心功能：
 * 1. 分页查询日志，支持多条件组合查询
 * 2. 异步保存日志，不阻塞主业务
 * 3. 支持导出日志到 Excel
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询操作日志
     * 
     * 查询逻辑：
     * 1. 构建查询条件（支持多条件组合）
     * 2. 执行分页查询
     * 3. 转换为 DTO 并填充操作人姓名
     */
    @Override
    public Page<OperationLogDTO> queryLogs(OperationLogQueryRequest request) {
        // 1. 构建分页对象
        Page<OperationLog> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<OperationLog> wrapper = buildQueryWrapper(request);

        // 3. 执行查询
        Page<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);

        // 4. 获取所有操作人ID，批量查询用户信息
        List<Long> operatorIds = logPage.getRecords().stream()
                .map(OperationLog::getOperatedBy)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userNameMap = getUserNameMap(operatorIds);

        // 5. 转换为 DTO
        Page<OperationLogDTO> dtoPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        dtoPage.setRecords(logPage.getRecords().stream()
                .map(log -> convertToDTO(log, userNameMap))
                .collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 根据ID查询日志详情
     */
    @Override
    public OperationLogDTO getLogById(Long logId) {
        OperationLog log = operationLogMapper.selectById(logId);
        if (log == null) {
            throw new BusinessException("日志不存在");
        }

        Map<Long, String> userNameMap = getUserNameMap(List.of(log.getOperatedBy()));
        return convertToDTO(log, userNameMap);
    }

    /**
     * 查询指定记录的操作历史
     */
    @Override
    public List<OperationLogDTO> getLogsByTarget(String module, Long targetId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getModule, module)
                .eq(OperationLog::getTargetId, targetId)
                .orderByDesc(OperationLog::getOperationTime);

        List<OperationLog> logs = operationLogMapper.selectList(wrapper);

        List<Long> operatorIds = logs.stream()
                .map(OperationLog::getOperatedBy)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userNameMap = getUserNameMap(operatorIds);

        return logs.stream()
                .map(log -> convertToDTO(log, userNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 同步保存操作日志
     */
    @Override
    @Transactional
    public void saveLog(OperationLog operationLog) {
        if (operationLog.getOperationTime() == null) {
            operationLog.setOperationTime(LocalDateTime.now());
        }
        operationLogMapper.insert(operationLog);
        log.debug("操作日志已保存: module={}, type={}, targetId={}",
                operationLog.getModule(), operationLog.getOperationType(), operationLog.getTargetId());
    }

    /**
     * 异步保存操作日志
     * 
     * 说明：
     * - 使用 @Async 注解，在独立线程中执行
     * - 不阻塞主业务流程
     * - 如果保存失败，只记录错误日志，不影响主业务
     */
    @Async
    @Override
    public void saveLogAsync(OperationLog logEntity) {
        try {
            // 检查必填字段 operatedBy
            if (logEntity.getOperatedBy() == null) {
                log.warn("操作日志保存跳过：operatedBy 为空，module={}, type={}",
                        logEntity.getModule(), logEntity.getOperationType());
                return;
            }

            if (logEntity.getOperationTime() == null) {
                logEntity.setOperationTime(LocalDateTime.now());
            }
            operationLogMapper.insert(logEntity);
            log.debug("异步操作日志已保存: module={}, type={}, targetId={}, operatedBy={}",
                    logEntity.getModule(), logEntity.getOperationType(),
                    logEntity.getTargetId(), logEntity.getOperatedBy());
        } catch (Exception e) {
            // 异步保存失败，只记录错误日志，不抛出异常
            log.error("异步保存操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 便捷方法：保存操作日志
     */
    @Override
    public void saveLog(String module, String operationType, Long targetId,
            String oldValue, String newValue, Long operatedBy, String ipAddress) {
        OperationLog log = OperationLog.builder()
                .module(module)
                .operationType(operationType)
                .targetId(targetId)
                .oldValue(oldValue)
                .newValue(newValue)
                .operatedBy(operatedBy)
                .ipAddress(ipAddress)
                .operationTime(LocalDateTime.now())
                .build();

        saveLogAsync(log);
    }

    /**
     * 导出操作日志
     */
    @Override
    public List<OperationLogDTO> exportLogs(OperationLogQueryRequest request) {
        // 导出时不分页，查询所有符合条件的记录
        LambdaQueryWrapper<OperationLog> wrapper = buildQueryWrapper(request);
        // 限制最大导出数量为10000条
        wrapper.last("LIMIT 10000");

        List<OperationLog> logs = operationLogMapper.selectList(wrapper);

        List<Long> operatorIds = logs.stream()
                .map(OperationLog::getOperatedBy)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userNameMap = getUserNameMap(operatorIds);

        return logs.stream()
                .map(log -> convertToDTO(log, userNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有模块列表
     */
    @Override
    public List<String> getAllModules() {
        return Arrays.asList(
                "sensitive_word",
                "sensitive_category",
                "problem_book",
                "publisher_whitelist",
                "purchased_problem_book",
                "user",
                "detection",
                "collection_book");
    }

    /**
     * 获取所有操作类型列表
     */
    @Override
    public List<String> getAllOperationTypes() {
        return Arrays.asList("create", "update", "delete", "import", "export", "login", "logout");
    }

    // ========== 私有方法 ==========

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<OperationLog> buildQueryWrapper(OperationLogQueryRequest request) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        // 模块（精确匹配）
        if (StringUtils.hasText(request.getModule())) {
            wrapper.eq(OperationLog::getModule, request.getModule());
        }

        // 操作类型（精确匹配）
        if (StringUtils.hasText(request.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, request.getOperationType());
        }

        // 操作人ID（精确匹配）
        if (request.getOperatedBy() != null) {
            wrapper.eq(OperationLog::getOperatedBy, request.getOperatedBy());
        }

        // 目标记录ID（精确匹配）
        if (request.getTargetId() != null) {
            wrapper.eq(OperationLog::getTargetId, request.getTargetId());
        }

        // 开始时间
        if (request.getStartTime() != null) {
            wrapper.ge(OperationLog::getOperationTime, request.getStartTime());
        }

        // 结束时间
        if (request.getEndTime() != null) {
            wrapper.le(OperationLog::getOperationTime, request.getEndTime());
        }

        // 按操作时间倒序排列
        wrapper.orderByDesc(OperationLog::getOperationTime);

        return wrapper;
    }

    /**
     * 批量获取用户姓名映射
     */
    private Map<Long, String> getUserNameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<SysUser> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getRealName, (a, b) -> a));
    }

    /**
     * 将实体转换为 DTO
     */
    private OperationLogDTO convertToDTO(OperationLog log, Map<Long, String> userNameMap) {
        return OperationLogDTO.builder()
                .logId(log.getLogId())
                .module(log.getModule())
                .moduleName(OperationLogDTO.getModuleName(log.getModule()))
                .operationType(log.getOperationType())
                .operationTypeName(OperationLogDTO.getOperationTypeName(log.getOperationType()))
                .targetId(log.getTargetId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .operatedBy(log.getOperatedBy())
                .operatorName(userNameMap.getOrDefault(log.getOperatedBy(), "未知用户"))
                .operationTime(log.getOperationTime())
                .ipAddress(log.getIpAddress())
                .build();
    }
}
