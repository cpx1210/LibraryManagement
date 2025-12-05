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
     * 敏感词分类ID（可选）
     * 关联 sensitive_categories 表
     * 如果不传，默认使用"通用"分类
     */
    private Long categoryId;

    /**
     * 检测类型（必填）
     * 关键词: 在书名、作者、内容简介等全部字段中检测
     * 书名: 仅在书名（题名）字段中检测
     * 作者: 仅在作者（著者）字段中检测
     */
    @NotBlank(message = "敏感词类别不能为空")
    private String detectionType = "关键词";

    /**
     * 警报信息（可选）
     * 命中该敏感词时显示的提示信息
     * 用于提供更详细的风险说明
     */
    @Size(max = 200, message = "警报信息长度不能超过200个字符")
    private String alertMessage;

    /**
     * 匹配类型（可选，默认为1-模糊匹配）
     * 0: 精确匹配
     * 1: 模糊匹配
     * 2: 正则表达式匹配
     */
    private Integer matchType = 1;

    /**
     * 风险等级（可选，默认为2-中风险）
     * 1: 低风险
     * 2: 中风险
     * 3: 高风险
     */
    private Integer riskLevel = 2;

    /**
     * 是否启用（可选，默认为true）
     */
    private Boolean isActive = true;
}
