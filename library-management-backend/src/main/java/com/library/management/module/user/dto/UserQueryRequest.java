package com.library.management.module.user.dto;

import lombok.Data;

/**
 * 用户查询请求参数
 * 用于分页查询和条件过滤
 */
@Data
public class UserQueryRequest {

    /**
     * 页码（默认第1页）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 真实姓名（模糊查询）
     */
    private String realName;

    /**
     * 角色（精确查询）
     */
    private String role;

    /**
     * 部门（模糊查询）
     */
    private String department;

    /**
     * 工号（精确查询）
     */
    private String employeeId;

    /**
     * 账号状态（0-禁用，1-启用）
     */
    private Integer isActive;
}
