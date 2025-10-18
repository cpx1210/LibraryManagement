package com.library.management.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户 DTO（数据传输对象）
 * 用于返回给前端，不包含敏感信息（如密码）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userId;
    private String username;
    private String role;
    private String department;
    private String realName;
    private String employeeId;
    private Integer isActive;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
}
