package com.library.management.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建请求
 * 用于新增用户时接收前端提交的数据
 *
 * 使用 Bean Validation 注解进行参数校验
 */
@Data
public class UserCreateRequest {

    /**
     * 用户名（必填，4-50字符，字母、数字、下划线）
     *
     * @NotBlank: 不能为空（包括 null、空字符串、空白字符）
     * @Size: 长度限制
     * @Pattern: 正则表达式校验
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须为4-50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    /**
     * 密码（必填，6-20字符）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须为6-20个字符")
    private String password;

    /**
     * 用户角色（必填，只能是 admin 或 user）
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(admin|user)$", message = "角色只能是 admin 或 user")
    private String role;

    /**
     * 真实姓名（必填）
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名最多50个字符")
    private String realName;

    /**
     * 部门（可选）
     */
    @Size(max = 100, message = "部门名称最多100个字符")
    private String department;

    /**
     * 工号（可选）
     */
    @Size(max = 50, message = "工号最多50个字符")
    private String employeeId;

    /**
     * 账号状态（可选，默认为1-启用）
     */
    private Integer isActive = 1;
}
