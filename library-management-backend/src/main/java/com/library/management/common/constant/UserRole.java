package com.library.management.common.constant;

/**
 * 用户角色常量类
 *
 *
 * 作用：定义系统中的所有用户角色
 *
 * 角色说明：
 * - ADMIN：管理员
 *   权限：用户管理、词库管理（增删改查）、书单检测、日志查询（全部）
 *
 * - USER：普通用户
 *   权限：词库查询、已购图书查询、书单检测、日志查询（仅个人）
 *
 * 使用场景：
 * 1. 用户注册/创建时指定角色
 * 2. Spring Security 权限验证（@PreAuthorize 注解）
 * 3. 数据库 sys_user 表的 role 字段值
 */
public class UserRole {

    /**
     * 管理员角色
     * 说明：拥有系统所有权限
     */
    public static final String ADMIN = "admin";

    /**
     * 普通用户角色
     * 说明：仅有查询和导出权限
     */
    public static final String USER = "user";

    /**
     * 私有构造函数，防止实例化
     */
    private UserRole() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
