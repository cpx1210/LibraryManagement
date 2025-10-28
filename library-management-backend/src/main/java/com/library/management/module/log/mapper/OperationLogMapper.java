package com.library.management.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志 Mapper 接口
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法
 *
 * @author Library Management System
 * @since 2025-10-27
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 根据模块查询操作日志
     *
     * @param module 操作模块
     * @return 操作日志列表
     */
    @Select("SELECT * FROM operation_log WHERE module = #{module} ORDER BY operation_time DESC")
    List<OperationLog> selectByModule(@Param("module") String module);

    /**
     * 根据操作人查询操作日志
     *
     * @param operatedBy 操作人ID
     * @return 操作日志列表
     */
    @Select("SELECT * FROM operation_log WHERE operated_by = #{operatedBy} ORDER BY operation_time DESC")
    List<OperationLog> selectByOperatedBy(@Param("operatedBy") Long operatedBy);

    /**
     * 根据操作类型查询操作日志
     *
     * @param operationType 操作类型
     * @return 操作日志列表
     */
    @Select("SELECT * FROM operation_log WHERE operation_type = #{operationType} ORDER BY operation_time DESC")
    List<OperationLog> selectByOperationType(@Param("operationType") String operationType);
}
