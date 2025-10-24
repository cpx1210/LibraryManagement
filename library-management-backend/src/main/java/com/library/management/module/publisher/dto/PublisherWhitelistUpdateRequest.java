package com.library.management.module.publisher.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 出版社白名单修改请求
 * 用于更新出版社白名单信息
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Data
public class PublisherWhitelistUpdateRequest {

    /**
     * 出版社ID（必填，用于指定要修改的出版社白名单记录）
     */
    @NotNull(message = "出版社ID不能为空")
    private Long publisherId;

    /**
     * 出版社名称（可选）
     */
    @Size(min = 1, max = 200, message = "出版社名称长度必须为1-200个字符")
    private String publisherName;

    /**
     * 年份批次（可选）
     */
    private Long years;

    /**
     * 是否启用（可选）
     */
    private Boolean isActive;
}
