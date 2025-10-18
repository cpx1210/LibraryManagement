package com.library.management.module.user.entity;

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
 * 用户实体类
 *
 * 对应数据库表：sys_user
 *
 * 功能说明：
 * 1. 存储用户基本信息（用户名、密码、角色等）
 * 2. 用于用户认证和授权
 * 3. 支持用户管理（增删改查）
 *
 * 注解说明：
 * - @TableName：MyBatis-Plus 注解，指定数据库表名
 * - @TableId：MyBatis-Plus 注解，标记主键字段，type = IdType.AUTO 表示主键自增
 * - @TableField：MyBatis-Plus 注解，映射数据库字段名（如果 Java 字段名与数据库列名不一致）
 * - @Data：Lombok 注解，自动生成 getter/setter、toString、equals、hashCode 方法
 * - @Builder：Lombok 注解，支持建造者模式创建对象
 * - @NoArgsConstructor：Lombok 注解，生成无参构造函数
 * - @AllArgsConstructor：Lombok 注解，生成全参构造函数
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUser {

    /**
     * 用户ID（主键，自增）
     *
     * 说明：
     * - 数据库字段：user_id BIGSERIAL PRIMARY KEY
     * - 主键自增，插入数据时不需要手动设置
     * - 类型：Long（对应数据库的 BIGINT）
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名（唯一，必填）
     *
     * 说明：
     * - 数据库字段：username VARCHAR(50) UNIQUE NOT NULL
     * - 用于登录
     * - 长度：4-50 个字符
     * - 格式：字母、数字、下划线
     */
    @TableField("username")
    private String username;

    /**
     * 密码哈希值（必填）
     *
     * 说明：
     * - 数据库字段：password_hash VARCHAR(255) NOT NULL
     * - 存储 BCrypt 加密后的密码（60 个字符）
     * - 加密方式：使用 PasswordUtil.encode() 方法
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 用户角色（必填）
     *
     * 说明：
     * - 数据库字段：role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'user'))
     * - 可选值：
     *   - admin：管理员（拥有所有权限）
     *   - user：普通用户（只读权限）
     * - Spring Security 会根据此字段控制权限
     */
    @TableField("role")
    private String role;

    /**
     * 部门（可选）
     */
    @TableField("department")
    private String department;

    /**
     * 真实姓名（必填）
     *
     * 说明：
     * - 数据库字段：real_name VARCHAR(50) NOT NULL
     * - 用户的真实姓名
     * - 用于显示在界面上、日志记录等
     */
    @TableField("real_name")
    private String realName;

    /**
     * 工号（唯一，可选）
     *
     * 说明：
     * - 数据库字段：employee_id VARCHAR(50) UNIQUE
     * - 员工工号
     * - 可用于生成用户名（如：emp_001）
     * - 可为空
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 账号状态（默认：1-启用）
     *
     * 说明：
     * - 数据库字段：is_active SMALLINT DEFAULT 1 CHECK (is_active IN (0, 1))
     * - 0：禁用（用户无法登录）
     * - 1：启用（正常状态）
     * - 用于账号启用/禁用功能
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 创建时间（自动生成）
     *
     * 说明：
     * - 数据库字段：create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * - 记录用户创建时间
     * - 插入数据时数据库自动设置当前时间
     * - Java 类型：LocalDateTime（对应 PostgreSQL 的 TIMESTAMP）
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 最后登录时间（可空）
     *
     * 说明：
     * - 数据库字段：last_login_time TIMESTAMP
     * - 记录用户最后一次登录的时间
     * - 每次登录成功后更新此字段
     * - 可为空（新创建的用户还未登录）
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
