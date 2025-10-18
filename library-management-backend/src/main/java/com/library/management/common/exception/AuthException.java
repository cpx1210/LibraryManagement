package com.library.management.common.exception;

import lombok.Getter;

/**
 * 认证/授权异常类
 *
 * 作用：处理用户认证和权限相关的异常
 *
 * 使用场景：
 * 1. 认证失败（401 Unauthorized）：
 *    - 用户未登录
 *    - Token 过期
 *    - Token 无效
 *    - 用户名或密码错误
 *
 * 2. 授权失败（403 Forbidden）：
 *    - 用户无权访问该资源
 *    - 用户角色不足（普通用户访问管理员接口）
 *    - 账号被禁用
 *
 * 与 BusinessException 的区别：
 * - AuthException：认证/授权错误（401/403状态码）
 * - BusinessException：业务逻辑错误（400/503状态码）
 *
 * 例如：
 * - 用户未登录访问需要认证的接口 → AuthException(401)
 * - 普通用户尝试删除其他用户 → AuthException(403)
 * - 用户不存在 → BusinessException(404)
 */
@Getter
public class AuthException extends RuntimeException {

    /**
     * 错误码
     * 默认为 401（未认证）
     */
    private final Integer code;

    /**
     * 构造函数：只传入错误消息，使用默认错误码 401
     *
     * @param message 错误消息
     */
    public AuthException(String message) {
        super(message);
        this.code = 401;
    }

    /**
     * 构造函数：传入错误码和错误消息
     *
     * 常用错误码：
     * - 401: 未认证（未登录、Token无效）
     * - 403: 无权限（权限不足）
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public AuthException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数：传入错误消息和原始异常
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.code = 401;
    }

    /**
     * 构造函数：传入错误码、错误消息和原始异常
     *
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原始异常
     */
    public AuthException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
