package com.library.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置类
 *
 * 功能说明：
 * 1. 配置 API 文档的基本信息（标题、描述、版本等）
 * 2. 配置 JWT 认证方式
 * 3. 配置作者联系方式
 *
 * 访问地址：
 * - Knife4j UI: http://localhost:8080/api/doc.html
 * - Swagger UI: http://localhost:8080/api/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8080/api/v3/api-docs
 *
 * @author Library Management System
 * @since 2025-10-25
 */
@Configuration
public class OpenApiConfig {

    /**
     * 创建 OpenAPI 实例
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(apiInfo())
                // 全局安全配置（JWT Token）
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    /**
     * API 基本信息配置
     *
     * @return API 信息对象
     */
    private Info apiInfo() {
        return new Info()
                .title("图书馆管理系统 API 文档")
                .description("图书馆管理系统后端接口文档，包含用户管理、敏感词管理、问题书目管理、出版社白名单等功能模块")
                .version("1.0.0")
                .contact(new Contact()
                        .name("图书馆管理系统开发团队")
                        .email("support@library.com")
                        .url("https://github.com/library-management"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    /**
     * 安全需求配置（全局应用）
     *
     * @return 安全需求对象
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer Token");
    }

    /**
     * 组件配置（包含安全方案）
     *
     * @return 组件对象
     */
    private Components components() {
        return new Components()
                .addSecuritySchemes("Bearer Token", securityScheme());
    }

    /**
     * JWT 安全方案配置
     *
     * 说明：定义如何在 Swagger UI 中使用 JWT Token
     * 使用方式：
     * 1. 先调用登录接口获取 Token
     * 2. 点击页面右上角的 "Authorize" 按钮
     * 3. 在弹窗中输入 Token（只需要输入 token 值，不需要加 Bearer 前缀）
     * 4. 点击 Authorize 后，后续所有请求都会自动携带 Token
     *
     * @return 安全方案对象
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("请输入 JWT Token（不需要加 Bearer 前缀）")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }
}
