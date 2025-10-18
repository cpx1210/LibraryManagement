package com.library.management.module.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录响应 DTO
 *
 * 功能说明：
 * - 用户登录成功后返回给前端的数据对象
 * - 包含 JWT Token 和用户基本信息
 * - 前端保存 Token，后续请求携带 Token 进行认证
 *
 * 前端使用方式：
 * 1. 登录成功后，保存 token 到 localStorage 或 sessionStorage
 * 2. 保存用户信息到 Pinia/Vuex 状态管理
 * 3. 后续请求在 Authorization 请求头中携带：Bearer {token}
 *
 * 注解说明：
 * - @Data：Lombok 注解，自动生成 getter/setter、toString、equals、hashCode
 * - @Builder：Lombok 注解，支持建造者模式创建对象
 * - @NoArgsConstructor：Lombok 注解，生成无参构造函数（JSON 序列化需要）
 * - @AllArgsConstructor：Lombok 注解，生成全参构造函数
 * - @JsonFormat：Jackson 注解，指定 JSON 序列化时的日期格式
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     *
     * 说明：
     * - 由后端生成的 JWT Token
     * - 前端保存到 localStorage 或 sessionStorage
     * - 后续请求需要在 Authorization 请求头中携带：Bearer {token}
     * - 格式：xxxxx.yyyyy.zzzzz（Header.Payload.Signature）
     *
     * 示例：
     * "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwidXNlcklkIjoxLCJyb2xlIjoiYWRtaW4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTY5ODM5ODQwMCwiZXhwIjoxNjk4NDA1NjAwfQ.abc123..."
     */
    private String token;

    /**
     * Token 类型（固定为 "Bearer"）
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token 过期时间（毫秒）
     *
     * 说明：
     * - Token 有效期（从配置文件读取，默认 2 小时 = 7200000 毫秒）
     * - 前端可以根据此值计算过期时间，提前刷新 Token
     * - 或者在请求失败时（401 Unauthorized）提示用户重新登录
     */
    private Long expiresIn;

    // ========== 用户信息 ==========

    /**
     * 用户 ID
     *
     * 说明：
     * - 用户的唯一标识
     * - 用于后续请求中的权限控制、日志记录等
     */
    private Long userId;

    /**
     * 用户名
     *
     * 说明：
     * - 用于登录的用户名
     * - 前端显示在界面上（如：欢迎，admin）
     */
    private String username;

    /**
     * 真实姓名
     *
     * 说明：
     * - 用户的真实姓名
     * - 前端显示在界面上（如：当前用户：张三）
     */
    private String realName;

    /**
     * 用户角色
     *
     * 说明：
     * - admin：管理员（拥有所有权限）
     * - user：普通用户（只读权限）
     * - 前端根据角色控制菜单显示、按钮权限等
     */
    private String role;

    /**
     * 部门
     *
     * 说明：
     * - 用户所属部门
     * - 前端可以显示在用户信息中
     */
    private String department;

    /**
     * 账号状态
     *
     * 说明：
     * - 0：禁用
     * - 1：启用
     * - 正常登录成功时应该是 1
     */
    private Integer isActive;

    /**
     * 最后登录时间
     *
     * 说明：
     * - 用户上一次登录的时间
     * - JSON 序列化格式：yyyy-MM-dd HH:mm:ss
     * - 前端可以显示在用户信息中
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
}
