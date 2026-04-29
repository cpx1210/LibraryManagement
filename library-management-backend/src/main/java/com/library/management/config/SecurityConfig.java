package com.library.management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置类
 *
 * 功能说明：
 * 1. 配置密码编码器（BCrypt）
 * 2. 配置 HTTP 安全规则（哪些接口需要认证）
 * 3. 集成 JWT 过滤器
 * 4. 配置 CORS 跨域
 * 5. 禁用 CSRF（前后端分离项目不需要）
 * 6. 配置会话管理（无状态 Stateless）
 *
 * 注解说明：
 * - @Configuration：标记为配置类
 * - @EnableWebSecurity：启用 Spring Security 的 Web 安全支持
 * - @EnableMethodSecurity：启用方法级别的安全控制（如 @PreAuthorize 注解）
 * - @RequiredArgsConstructor：Lombok 自动生成包含 final 字段的构造函数
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 认证过滤器
     * 说明：通过构造函数注入，由 Spring 容器自动创建和管理
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器 Bean
     *
     * 作用：
     * 1. 用户注册时加密密码
     * 2. 用户登录时验证密码
     * 3. Spring Security 会自动使用此 Bean
     *
     * BCrypt 优势：
     * - 单向加密，无法解密
     * - 自动生成随机盐
     * - 防止彩虹表攻击
     * - 速度较慢，防止暴力破解
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     *
     * 这是 Spring Security 的核心配置，定义了整个应用的安全规则。
     *
     * 配置内容：
     * 1. CSRF 保护：禁用（因为使用 JWT，不需要 CSRF Token）
     * 2. CORS 跨域：启用（允许前端跨域请求）
     * 3. 授权规则：配置哪些接口需要认证，哪些可以匿名访问
     * 4. 会话管理：无状态（STATELESS），不使用 Session
     * 5. JWT 过滤器：添加到 Spring Security 过滤器链中
     *
     * @param http HttpSecurity 对象（Spring Security 提供）
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== 1. 禁用 CSRF ==========
                // 说明：CSRF（跨站请求伪造）防护对于前后端分离项目不需要
                // 原因：我们使用 JWT Token 认证，不使用 Cookie/Session
                .csrf(AbstractHttpConfigurer::disable)

                // ========== 2. 配置 CORS 跨域 ==========
                // 说明：允许前端（Vue）从不同域名访问后端 API
                // 例如：前端运行在 localhost:5173，后端运行在 localhost:8080
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ========== 3. 配置授权规则 ==========
                // 说明：定义哪些接口需要认证，哪些可以匿名访问
                .authorizeHttpRequests(auth -> auth
                        // 白名单：这些接口不需要认证，任何人都可以访问
                        .requestMatchers(
                                "/auth/login", // 登录接口
                                "/auth/register", // 注册接口（如果有）
                                "/booklist-check/upload", // 公开上传书单
                                "/booklist-check/template", // 公开下载模板
                                "/actuator/health", // 健康检查接口（Docker/K8s 探针）
                                "/actuator/info", // 应用信息接口
                                "/actuator/**", // Actuator 所有端点（可根据需要调整范围）
                                "/doc.html", // Knife4j API 文档页面
                                "/swagger-ui.html", // Swagger UI 页面
                                "/swagger-ui/**", // Swagger UI 静态资源
                                "/v3/api-docs/**", // OpenAPI 文档
                                "/swagger-resources/**", // Swagger 资源
                                "/webjars/**", // Swagger 依赖的 webjars
                                "/favicon.ico", // 网站图标
                                "/error" // 错误页面
                        ).permitAll()

                        // 其他所有接口都需要认证
                        // 说明：没有在白名单中的接口，必须携带有效的 JWT Token 才能访问
                        .anyRequest().authenticated())

                // ========== 4. 配置会话管理策略 ==========
                // 说明：设置为 STATELESS（无状态）
                // 原因：JWT 是无状态的，不需要服务器保存 Session
                // 好处：可以水平扩展，多台服务器之间不需要共享 Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ========== 5. 添加 JWT 认证过滤器 ==========
                // 说明：将我们自定义的 JwtAuthenticationFilter 添加到 Spring Security 过滤器链
                // 位置：在 UsernamePasswordAuthenticationFilter 之前执行
                // 作用：在用户名密码认证之前，先检查 JWT Token 是否有效
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 跨域配置源
     *
     * CORS (Cross-Origin Resource Sharing) 跨域资源共享
     * 说明：浏览器的同源策略限制了不同域名之间的请求，需要配置 CORS 允许跨域
     * 
     * @return CorsConfigurationSource CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（前端地址）
        // 说明：允许哪些域名访问后端 API
        // 开发环境：允许所有域名（*）
        // 生产环境：应该设置为具体的前端域名，如 https://library.example.com
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头
        // 说明：允许前端发送哪些请求头
        // * 表示允许所有请求头（包括自定义的 Authorization 等）
        configuration.setAllowedHeaders(List.of("*"));

        // 允许发送凭证（Cookies）
        // 说明：如果前端需要发送 Cookie，需要设置为 true
        // 注意：设置为 true 时，allowedOrigins 不能使用 *，必须指定具体域名
        configuration.setAllowCredentials(true);

        // 暴露的响应头
        // 说明：允许前端 JavaScript 读取哪些响应头
        // Authorization：用于返回新的 Token（如 Token 刷新场景）
        configuration.setExposedHeaders(List.of("Authorization"));

        // 预检请求的缓存时间（秒）
        // 说明：OPTIONS 预检请求的结果可以缓存 1 小时
        // 作用：减少预检请求的次数，提升性能
        configuration.setMaxAge(3600L);

        // 应用 CORS 配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
