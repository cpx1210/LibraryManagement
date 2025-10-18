package com.library.management.common.constant;

/**
 * 响应码常量类
 *
 * 作用：统一定义系统中所有的响应状态码
 *
 * 设计说明：
 * - 2xx: 成功状态码
 * - 4xx: 客户端错误（请求参数错误、权限不足等）
 * - 5xx: 服务端错误（系统异常、数据库错误等）
 *
 * 使用场景：
 * 1. Controller 返回响应时使用
 * 2. 全局异常处理器根据异常类型返回对应状态码
 * 3. 前端根据状态码进行不同的处理
 */
public class ResponseCode {

    // ==================== 成功状态码 ====================

    /**
     * 操作成功
     */
    public static final Integer SUCCESS = 200;

    // ==================== 客户端错误 4xx ====================

    /**
     * 请求参数错误（通用）
     */
    public static final Integer BAD_REQUEST = 400;

    /**
     * 未认证（未登录）
     */
    public static final Integer UNAUTHORIZED = 401;

    /**
     * 无权限访问
     */
    public static final Integer FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    public static final Integer NOT_FOUND = 404;

    /**
     * 请求方法不支持
     */
    public static final Integer METHOD_NOT_ALLOWED = 405;

    /**
     * 参数校验失败
     */
    public static final Integer VALIDATION_FAILED = 422;

    // ==================== 服务端错误 5xx ====================

    /**
     * 服务器内部错误（通用）
     */
    public static final Integer INTERNAL_SERVER_ERROR = 500;

    /**
     * 数据库操作失败
     */
    public static final Integer DATABASE_ERROR = 501;

    /**
     * 文件操作失败
     */
    public static final Integer FILE_ERROR = 502;

    /**
     * 业务逻辑错误
     */
    public static final Integer BUSINESS_ERROR = 503;

    // ==================== 私有构造函数 ====================

    /**
     * 私有构造函数，防止实例化
     * 说明：这是一个工具类，只提供静态常量，不需要创建实例
     */
    private ResponseCode() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
