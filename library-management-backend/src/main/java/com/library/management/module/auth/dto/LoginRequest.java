package com.library.management.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求 DTO
 *
 * 功能说明：
 * - 前端发送登录请求时的数据对象
 * - 包含用户名和密码
 * - 使用 Bean Validation 进行参数校验
 *
 * DTO (Data Transfer Object) 说明：
 * - 用于前后端数据传输
 * - 与实体类（Entity）分离，避免暴露数据库结构
 * - 可以根据业务需求灵活定义字段
 *
 * 注解说明：
 * - @Data：Lombok 注解，自动生成 getter/setter、toString、equals、hashCode
 * - @Builder：Lombok 注解，支持建造者模式创建对象
 * - @NoArgsConstructor：Lombok 注解，生成无参构造函数（JSON 反序列化需要）
 * - @AllArgsConstructor：Lombok 注解，生成全参构造函数
 * - @NotBlank：Bean Validation 注解，校验字符串不能为空（null、空字符串、只有空格都不行）
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 用户名（必填）
     *
     * 校验规则：
     * - 不能为 null
     * - 不能为空字符串
     * - 不能只包含空格
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（必填）
     *
     * 校验规则：
     * - 不能为 null
     * - 不能为空字符串
     * - 不能只包含空格
     *
     * 说明：
     * - 前端发送的是明文密码
     * - 后端会使用 PasswordUtil.matches() 验证密码
     * - 密码在传输过程中应该使用 HTTPS 加密
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
