package com.library.management.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户修改请求
 * 用于更新用户信息（不包含密码修改）
 *
 * 注意：密码修改通过单独的密码重置接口进行
 */
@Data
public class UserUpdateRequest {

    /**
     * 用户ID（必填，用于指定要修改的用户）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 用户名（可选，如果修改会检查唯一性）
     */
    @Size(min = 4, max = 50, message = "用户名长度必须为4-50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    /**
     * 用户角色（可选）
     */
    @Pattern(regexp = "^(admin|user)$", message = "角色只能是 admin 或 user")
    private String role;

    /**
     * 真实姓名（可选）
     */
    @Size(max = 50, message = "真实姓名最多50个字符")
    private String realName;

    /**
     * 部门（可选）
     */
    @Size(max = 100, message = "部门名称最多100个字符")
    private String department;

    /**
     * 工号（可选，如果修改会检查唯一性）
     */
    @Size(max = 50, message = "工号最多50个字符")
    private String employeeId;

    /**
     * 账号状态（可选）
     */
    private Integer isActive;
}
