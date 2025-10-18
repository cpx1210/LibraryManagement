package com.library.management.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.exception.BusinessException;
import com.library.management.common.utils.PasswordUtil;
import com.library.management.module.user.dto.UserCreateRequest;
import com.library.management.module.user.dto.UserDTO;
import com.library.management.module.user.dto.UserQueryRequest;
import com.library.management.module.user.dto.UserUpdateRequest;
import com.library.management.module.user.entity.SysUser;
import com.library.management.module.user.mapper.UserMapper;
import com.library.management.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 *
 * @Service: Spring 服务层注解，标记为业务逻辑组件
 * @RequiredArgsConstructor: Lombok 注解，自动生成包含 final 字段的构造函数（用于依赖注入）
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 分页查询用户列表
     *
     * 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件
     * - like: 模糊查询
     * - eq: 精确查询
     */
    @Override
    public Page<UserDTO> queryUsers(UserQueryRequest request) {
        // 创建分页对象
        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysUser::getUsername, request.getUsername());
        }

        // 真实姓名模糊查询
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(SysUser::getRealName, request.getRealName());
        }

        // 角色精确查询
        if (StringUtils.hasText(request.getRole())) {
            wrapper.eq(SysUser::getRole, request.getRole());
        }

        // 部门模糊查询
        if (StringUtils.hasText(request.getDepartment())) {
            wrapper.like(SysUser::getDepartment, request.getDepartment());
        }

        // 工号精确查询
        if (StringUtils.hasText(request.getEmployeeId())) {
            wrapper.eq(SysUser::getEmployeeId, request.getEmployeeId());
        }

        // 账号状态查询
        if (request.getIsActive() != null) {
            wrapper.eq(SysUser::getIsActive, request.getIsActive());
        }

        // 按创建时间倒序排序
        wrapper.orderByDesc(SysUser::getCreateTime);

        // 执行分页查询
        Page<SysUser> userPage = userMapper.selectPage(page, wrapper);

        // 转换为 DTO（不包含密码）
        Page<UserDTO> dtoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        dtoPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList());

        return dtoPage;
    }

    /**
     * 根据ID查询用户详情
     */
    @Override
    public UserDTO getUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToDTO(user);
    }

    /**
     * 创建新用户
     *
     * 业务逻辑：
     * 1. 检查用户名是否已存在
     * 2. 检查工号是否已存在（如果提供了工号）
     * 3. 使用 BCrypt 加密密码
     * 4. 创建用户对象并保存到数据库
     */
    @Override
    public UserDTO createUser(UserCreateRequest request) {
        // 1. 检查用户名是否已存在
        SysUser existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查工号是否已存在（如果提供了工号）
        if (StringUtils.hasText(request.getEmployeeId())) {
            SysUser userWithEmployeeId = userMapper.selectByEmployeeId(request.getEmployeeId());
            if (userWithEmployeeId != null) {
                throw new BusinessException("工号已存在");
            }
        }

        // 3. 创建用户对象
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .passwordHash(PasswordUtil.encode(request.getPassword())) // 加密密码
                .role(request.getRole())
                .realName(request.getRealName())
                .department(request.getDepartment())
                .employeeId(request.getEmployeeId())
                .isActive(request.getIsActive())
                .build();

        // 4. 保存到数据库
        int rows = userMapper.insert(user);
        if (rows == 0) {
            throw new BusinessException("创建用户失败");
        }

        // 5. 返回用户信息（不包含密码）
        return convertToDTO(user);
    }

    /**
     * 修改用户信息
     *
     * 业务逻辑：
     * 1. 检查用户是否存在
     * 2. 如果修改用户名，检查新用户名是否已被其他用户占用
     * 3. 如果修改工号，检查新工号是否已被其他用户占用
     * 4. 更新用户信息（只更新非空字段）
     *
     * 注意：此接口不修改密码，密码通过单独的密码重置接口修改
     */
    @Override
    public UserDTO updateUser(UserUpdateRequest request) {
        // 1. 检查用户是否存在
        SysUser user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 如果修改了用户名，检查新用户名是否已被其他用户占用
        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            SysUser existingUser = userMapper.selectByUsername(request.getUsername());
            if (existingUser != null) {
                throw new BusinessException("用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        // 3. 如果修改了工号，检查新工号是否已被其他用户占用
        if (StringUtils.hasText(request.getEmployeeId()) && !request.getEmployeeId().equals(user.getEmployeeId())) {
            SysUser userWithEmployeeId = userMapper.selectByEmployeeId(request.getEmployeeId());
            if (userWithEmployeeId != null) {
                throw new BusinessException("工号已被占用");
            }
            user.setEmployeeId(request.getEmployeeId());
        }

        // 4. 更新其他字段（只更新非空字段）
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // 5. 保存到数据库
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException("修改用户失败");
        }

        // 6. 返回修改后的用户信息
        return convertToDTO(user);
    }

    /**
     * 删除用户
     *
     * 业务逻辑：
     * 1. 检查用户是否存在
     * 2. 执行物理删除（从数据库中删除记录）
     *
     * 注意：这是物理删除，数据无法恢复
     * 如果需要保留数据，建议使用禁用功能（设置 isActive=0）
     */
    @Override
    public void deleteUser(Long userId) {
        // 1. 检查用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 执行删除
        int rows = userMapper.deleteById(userId);
        if (rows == 0) {
            throw new BusinessException("删除用户失败");
        }
    }

    /**
     * 重置用户密码
     *
     * 业务逻辑：
     * 1. 检查用户是否存在
     * 2. 使用 BCrypt 加密新密码
     * 3. 更新数据库中的密码哈希值
     *
     * 使用场景：
     * - 管理员为用户重置密码
     * - 用户忘记密码时重置
     */
    @Override
    public void resetPassword(Long userId, String newPassword) {
        // 1. 检查用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 加密新密码
        String encodedPassword = PasswordUtil.encode(newPassword);

        // 3. 更新密码
        user.setPasswordHash(encodedPassword);
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException("重置密码失败");
        }
    }

    /**
     * 实体转 DTO（过滤密码）
     */
    private UserDTO convertToDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
