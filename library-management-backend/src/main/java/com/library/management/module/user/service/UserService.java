package com.library.management.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.user.dto.UserCreateRequest;
import com.library.management.module.user.dto.UserDTO;
import com.library.management.module.user.dto.UserQueryRequest;
import com.library.management.module.user.dto.UserUpdateRequest;

/**
 * 用户服务接口
 * 定义用户管理的业务方法
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Page<UserDTO> queryUsers(UserQueryRequest request);

    /**
     * 根据ID查询用户详情
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUserById(Long userId);

    /**
     * 创建新用户
     *
     * @param request 用户创建请求
     * @return 创建的用户信息
     */
    UserDTO createUser(UserCreateRequest request);

    /**
     * 修改用户信息
     *
     * @param request 用户修改请求
     * @return 修改后的用户信息
     */
    UserDTO updateUser(UserUpdateRequest request);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);
}
