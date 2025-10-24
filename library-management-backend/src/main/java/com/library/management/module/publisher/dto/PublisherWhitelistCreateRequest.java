package com.library.management.module.publisher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 出版社白名单创建请求
 * 用于新增出版社白名单时接收前端提交的数据
 *
 * 使用 Bean Validation 注解进行参数校验
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Data
public class PublisherWhitelistCreateRequest {

    /**
     * 出版社名称（必填，1-200字符）
     */
    @NotBlank(message = "出版社名称不能为空")
    @Size(min = 1, max = 200, message = "出版社名称长度必须为1-200个字符")
    private String publisherName;

    /**
     * 年份批次（可选）
     * 注意：业务逻辑待明确，暂时不做校验
     */
    private Long years;

    /**
     * 是否启用（可选，默认为 true）
     */
    private Boolean isActive;
}
