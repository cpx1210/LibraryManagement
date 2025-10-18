package com.library.management.module.auth.service;

import com.library.management.module.auth.dto.LoginRequest;
import com.library.management.module.auth.dto.LoginResponse;

/**
 * 认证服务接口
 *
 * 功能说明：
 * - 用户登录认证
 * - 生成 JWT Token
 * - 用户登出（可选）
 *
 * 设计模式：
 * - 面向接口编程
 * - 接口定义方法签名，实现类提供具体逻辑
 * - 便于单元测试和代码解耦
 *
 * @author Library Management System
 * @since 2025-10-15
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * 业务流程：
     * 1. 根据用户名查询用户
     * 2. 验证用户是否存在
     * 3. 验证账号是否被禁用
     * 4. 验证密码是否正确
     * 5. 生成 JWT Token
     * 6. 更新最后登录时间
     * 7. 返回 Token 和用户信息
     *
     * 异常情况：
     * - 用户不存在：抛出 BusinessException("用户名或密码错误")
     * - 密码错误：抛出 BusinessException("用户名或密码错误")
     * - 账号被禁用：抛出 BusinessException("账号已被禁用")
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录响应（Token + 用户信息）
     */
    LoginResponse login(LoginRequest request);
}
