package com.library.management.module.auth.service.impl;

import com.library.management.common.exception.BusinessException;
import com.library.management.common.utils.JwtUtil;
import com.library.management.common.utils.PasswordUtil;
import com.library.management.module.auth.dto.LoginRequest;
import com.library.management.module.auth.dto.LoginResponse;
import com.library.management.module.auth.service.AuthService;
import com.library.management.module.user.entity.SysUser;
import com.library.management.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 *
 * 功能说明：
 * - 实现用户登录业务逻辑
 * - 验证用户名和密码
 * - 生成 JWT Token
 * - 更新用户最后登录时间
 *
 * 注解说明：
 * - @Service：Spring 注解，标记为服务层组件，自动注册到 Spring 容器
 * - @Slf4j：Lombok 注解，自动生成 log 日志对象
 * - @RequiredArgsConstructor：Lombok 注解，自动生成包含 final 字段的构造函数（依赖注入）
 * - @Transactional：Spring 事务注解，方法执行失败时自动回滚
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 用户 Mapper（数据访问层）
     * 说明：通过构造函数注入，由 Spring 自动装配
     */
    private final UserMapper userMapper;

    /**
     * JWT 工具类
     * 说明：用于生成和验证 JWT Token
     */
    private final JwtUtil jwtUtil;

    /**
     * Token 过期时间（从配置文件读取）
     * 说明：默认 7200000 毫秒（2 小时）
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * 用户登录
     *
     * 业务流程详解：
     * 1. 根据用户名查询用户 → 调用 userMapper.selectByUsername()
     * 2. 验证用户是否存在 → 不存在抛出异常
     * 3. 验证账号是否被禁用 → isActive = 0 抛出异常
     * 4. 验证密码是否正确 → 使用 PasswordUtil.matches() 验证
     * 5. 生成 JWT Token → 调用 jwtUtil.generateToken()
     * 6. 更新最后登录时间 → 调用 userMapper.updateLastLoginTime()
     * 7. 构建并返回登录响应 → 使用 Builder 模式构建 LoginResponse
     *
     * 安全说明：
     * - 用户名和密码错误时，统一返回"用户名或密码错误"，不透露具体哪个字段错误
     * - 密码使用 BCrypt 验证，无法反向解密
     * - 登录成功后记录日志，便于审计
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录响应（Token + 用户信息）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        // 记录登录尝试日志（不记录密码）
        log.info("用户登录尝试：username={}", request.getUsername());

        // ========== 1. 根据用户名查询用户 ==========
        SysUser user = userMapper.selectByUsername(request.getUsername());

        // ========== 2. 验证用户是否存在 ==========
        if (user == null) {
            log.warn("登录失败：用户不存在，username={}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // ========== 3. 验证账号是否被禁用 ==========
        // isActive = 0 表示禁用，1 表示启用
        if (user.getIsActive() == null || user.getIsActive() == 0) {
            log.warn("登录失败：账号已被禁用，username={}, userId={}",
                    request.getUsername(), user.getUserId());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // ========== 4. 验证密码是否正确 ==========
        // 使用 PasswordUtil.matches() 验证明文密码与数据库中的加密密码
        // request.getPassword()：用户输入的明文密码
        // user.getPasswordHash()：数据库中存储的 BCrypt 加密密码
        boolean passwordMatches = PasswordUtil.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            log.warn("登录失败：密码错误，username={}, userId={}",
                    request.getUsername(), user.getUserId());
            throw new BusinessException("用户名或密码错误");
        }

        // ========== 5. 生成 JWT Token ==========
        // 将用户信息（用户名、用户ID、角色）编码到 Token 中
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getUserId(),
                user.getRole()
        );

        // ========== 6. 更新最后登录时间 ==========
        // 异步更新，不影响登录响应速度
        // 使用 @Transactional 保证数据一致性
        userMapper.updateLastLoginTime(user.getUserId());

        // ========== 7. 构建登录响应 ==========
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .department(user.getDepartment())
                .isActive(user.getIsActive())
                .lastLoginTime(user.getLastLoginTime())
                .build();

        // 记录登录成功日志
        log.info("用户登录成功：username={}, userId={}, role={}",
                user.getUsername(), user.getUserId(), user.getRole());

        return response;
    }
}
