package com.library.management.module.auth.controller;

import com.library.management.common.result.Result;
import com.library.management.module.auth.dto.LoginRequest;
import com.library.management.module.auth.dto.LoginResponse;
import com.library.management.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * 功能：用户登录、登出
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Tag(name = "认证管理", description = "用户登录、登出等认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录响应（Token + 用户信息）
     */
    @Operation(summary = "用户登录", description = "通过用户名和密码登录系统，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }
}
