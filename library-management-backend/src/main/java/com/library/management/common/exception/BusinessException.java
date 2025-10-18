package com.library.management.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * 作用：处理业务逻辑中的异常情况
 *
 * 使用场景：
 * 1. 数据不存在：用户ID不存在、敏感词不存在
 * 2. 业务规则违反：重复添加、状态不允许操作
 * 3. 数据校验失败：Excel格式错误、ISBN格式错误
 *
 * 例如：
 * - 删除用户时，用户不存在
 * - 添加敏感词时，敏感词已存在
 * - 导入Excel时，数据格式不符合要求
 *
 * 与 AuthException 的区别：
 * - BusinessException：业务逻辑错误（400/503状态码）
 * - AuthException：认证/授权错误（401/403状态码）
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     * 默认为 503（业务逻辑错误）
     */
    private final Integer code;

    /**
     * 构造函数：只传入错误消息，使用默认错误码 503
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 503;
    }

    /**
     * 构造函数：传入错误码和错误消息
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数：传入错误消息和原始异常
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 503;
    }

    /**
     * 构造函数：传入错误码、错误消息和原始异常
     *
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
