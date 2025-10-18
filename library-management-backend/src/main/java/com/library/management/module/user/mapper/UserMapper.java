package com.library.management.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 *
 * 数据访问层（DAO），负责用户表的数据库操作
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法（如根据用户名查询）
 * 3. 支持复杂的 SQL 查询
 *
 * BaseMapper 提供的基础方法：
 * - insert(SysUser user)：插入一条记录
 * - deleteById(Long id)：根据 ID 删除
 * - updateById(SysUser user)：根据 ID 更新
 * - selectById(Long id)：根据 ID 查询
 * - selectList(Wrapper<SysUser> wrapper)：条件查询列表
 * - selectPage(Page<SysUser> page, Wrapper<SysUser> wrapper)：分页查询
 * - 还有更多方法...
 *
 * 注解说明：
 * - @Mapper：MyBatis 注解，标记为 Mapper 接口，Spring 会自动扫描并创建代理对象
 * - @Select：MyBatis 注解，定义查询 SQL
 * - @Update：MyBatis 注解，定义更新 SQL
 * - @Param：MyBatis 注解，指定 SQL 中的参数名
 *
 * @author Library Management System
 * @since 2025-10-15
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据工号查询用户
     */
    @Select("SELECT * FROM sys_user WHERE employee_id = #{employeeId}")
    SysUser selectByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 更新用户最后登录时间
     */
    @Update("UPDATE sys_user SET last_login_time = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateLastLoginTime(@Param("userId") Long userId);

    /**
     * 更新用户状态（启用/禁用）
     */
    @Update("UPDATE sys_user SET is_active = #{isActive} WHERE user_id = #{userId}")
    int updateUserStatus(@Param("userId") Long userId, @Param("isActive") Integer isActive);
}
