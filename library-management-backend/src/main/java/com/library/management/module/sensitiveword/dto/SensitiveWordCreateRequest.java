package com.library.management.module.sensitiveword.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 敏感词创建请求
 * 用于新增敏感词时接收前端提交的数据
 *
 * 使用 Bean Validation 注解进行参数校验
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Data
public class SensitiveWordCreateRequest {

    /**
     * 敏感词内容（必填，1-100字符）
     */
    @NotBlank(message = "敏感词内容不能为空")
    @Size(min = 1, max = 100, message = "敏感词内容长度必须为1-100个字符")
    private String keyword;

    /**
     * 敏感词类别（必填，最多50字符）
     */
    @NotBlank(message = "敏感词类别不能为空")
    @Size(max = 50, message = "类别名称最多50个字符")
    private String category;
}
