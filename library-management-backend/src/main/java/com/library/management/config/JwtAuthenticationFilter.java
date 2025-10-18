package com.library.management.config;

import com.library.management.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 *
 * 功能说明：
 * 1. 拦截每个 HTTP 请求
 * 2. 从请求头中提取 JWT token
 * 3. 验证 token 的有效性
 * 4. 将用户信息设置到 Spring Security 上下文
 *
 * 继承 OncePerRequestFilter：确保每个请求只执行一次
 *
 * @author Library Management System
 * @since 2025-10-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
        private final JwtUtil jwtUtil;
        /**
         * JWT token 在请求头中的键名
         */
        private static final String AUTHORIZATION_HEADER = "Authorization";

        /**
         * JWT token 的前缀
         */
        private static final String BEARER_PREFIX = "Bearer ";

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response,
                        @NonNull FilterChain filterChain) throws ServletException, IOException {
                try {
                        // 1. 从请求头获取 JWT token
                        String token = extractTokenFromRequest(request);

                        // 2. 如果 token 存在且有效，则设置认证信息
                        if (token != null &&
                                        SecurityContextHolder.getContext().getAuthentication() == null) {
                                authenticateUser(token, request);
                        }
                } catch (Exception e) {
                        log.error("JWT 认证失败: {}", e.getMessage());
                        // 认证失败不抛出异常，继续执行过滤器链，让 SpringSecurity 处理
                }

                // 3. 继续执行过滤器链
                filterChain.doFilter(request, response);
        }

        /**
         * 从请求头中提取 JWT token
         *
         * @param request HTTP 请求
         * @return JWT token，如果不存在或格式不正确则返回 null
         */
        private String extractTokenFromRequest(HttpServletRequest request) {
                String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

                if (StringUtils.hasText(bearerToken) &&
                                bearerToken.startsWith(BEARER_PREFIX)) {
                        return bearerToken.substring(BEARER_PREFIX.length());
                }

                return null;
        }

        /**
         * 验证 token 并设置认证信息到 SecurityContext
         *
         * @param token   JWT token
         * @param request HTTP 请求
         */
        private void authenticateUser(String token,
                        HttpServletRequest request) {
                // 1. 从 token 中提取用户名
                String username = jwtUtil.getUsernameFromToken(token);

                if (username != null) {
                        // 2. 验证 token 是否有效
                        if (jwtUtil.validateToken(token, username)) {
                                // 3. 从 token 中提取角色
                                String role = jwtUtil.getRoleFromToken(token);

                                // 4. 创建权限集合（角色需要加 ROLE_ 前缀）
                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                                // 5. 创建认证对象
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                username,
                                                null,

                                                Collections.singletonList(authority));

                                // 6. 设置请求详情
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                // 7. 将认证信息设置到 SecurityContext
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                log.debug("JWT 认证成功，用户: {}, 角色: {}",
                                                username, role);
                        } else {
                                log.warn("JWT token 验证失败，用户: {}",
                                                username);
                        }
                }
        }
}