package com.library.management.module.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作台账实体类
 *
 * 对应数据库表：operation_log
 *
 * 功能说明：
 * 1. 记录系统所有关键操作的台账
 * 2. 支持操作审计和追溯
 * 3. 记录操作前后的数据变更
 *
 * @author Library Management System
 * @since 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("operation_log")
public class OperationLog {

    /**
     * 日志唯一标识（主键，自增）
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 操作模块（必填）
     *
     * 说明：
     * - 数据库字段：module VARCHAR(50) NOT NULL
     * - 记录操作所属的模块，如：purchased_problem_books、sensitive_words等
     */
    @TableField("module")
    private String module;

    /**
     * 操作类型（必填）
     *
     * 说明：
     * - 数据库字段：operation_type VARCHAR(20) NOT NULL
     * - 可选值：
     *   - create：新增
     *   - update：修改
     *   - delete：删除
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作目标ID（可选）
     *
     * 说明：
     * - 数据库字段：target_id BIGINT
     * - 记录被操作记录的主键ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 操作前内容（可选）
     *
     * 说明：
     * - 数据库字段：old_value TEXT
     * - JSON格式存储变更前的数据
     */
    @TableField("old_value")
    private String oldValue;

    /**
     * 操作后内容（可选）
     *
     * 说明：
     * - 数据库字段：new_value TEXT
     * - JSON格式存储变更后的数据
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 操作人ID（必填）
     *
     * 说明：
     * - 数据库字段：operated_by BIGINT NOT NULL
     * - 记录执行操作的用户ID
     * - 外键关联 sys_user.user_id
     */
    @TableField("operated_by")
    private Long operatedBy;

    /**
     * 操作时间（自动生成）
     *
     * 说明：
     * - 数据库字段：operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * - 记录操作发生的时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 操作IP地址（可选）
     *
     * 说明：
     * - 数据库字段：ip_address VARCHAR(45)
     * - 记录操作来源的IP地址（支持IPv4和IPv6）
     */
    @TableField("ip_address")
    private String ipAddress;
}
