package com.library.management.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.result.Result;
import com.library.management.module.user.dto.PasswordResetRequest;
import com.library.management.module.user.dto.UserCreateRequest;
import com.library.management.module.user.dto.UserDTO;
import com.library.management.module.user.dto.UserQueryRequest;
import com.library.management.module.user.dto.UserUpdateRequest;
import com.library.management.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @Tag: Swagger 文档标签
 * @RestController: 组合注解，等同于 @Controller + @ResponseBody
 * @RequestMapping: 定义基础路径为 /users
 * @RequiredArgsConstructor: Lombok 注解，自动注入 final 字段
 */
@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param request 查询条件（自动绑定查询参数）
     * @return 分页结果
     */
    @Operation(summary = "查询用户列表", description = "支持分页和多条件查询")
    @GetMapping
    public Result<Page<UserDTO>> queryUsers(UserQueryRequest request) {
        Page<UserDTO> page = userService.queryUsers(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询用户详情
     *
     * @param userId 用户ID（路径参数）
     * @return 用户信息
     */
    @Operation(summary = "查询用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{userId}")
    public Result<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 创建新用户
     *
     * @param request 用户创建请求（JSON 请求体）
     * @return 创建的用户信息
     *
     * @Valid: 启用 Bean Validation 参数校验
     * @RequestBody: 接收 JSON 格式的请求体
     */
    @Operation(summary = "创建新用户", description = "新增用户，密码会自动加密")
    @PostMapping
    public Result<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDTO user = userService.createUser(request);
        return Result.success("用户创建成功", user);
    }

    /**
     * 修改用户信息
     *
     * @param request 用户修改请求（JSON 请求体）
     * @return 修改后的用户信息
     *
     *         注意：此接口不修改密码，密码通过单独的密码重置接口修改
     */
    @Operation(summary = "修改用户信息", description = "更新用户信息（不包含密码修改）")
    @PostMapping("/update")
    public Result<UserDTO> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        UserDTO user = userService.updateUser(request);
        return Result.success("用户修改成功", user);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID（路径参数）
     * @return 删除结果
     *
     *         注意：这是物理删除，数据无法恢复
     *         建议使用禁用功能代替删除（调用修改接口设置 isActive=0）
     */
    @Operation(summary = "删除用户", description = "物理删除用户（不可恢复）")
    @PostMapping("/delete/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("用户删除成功", null);
    }

    /**
     * 重置用户密码
     *
     * @param request 密码重置请求（JSON 请求体）
     * @return 重置结果
     *
     *         使用场景：
     *         - 管理员为用户重置密码
     *         - 用户忘记密码时重置
     */
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(request.getUserId(), request.getNewPassword());
        return Result.success("密码重置成功", null);
    }
}
