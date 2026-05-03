package com.library.management.common.exception;

import com.library.management.common.constant.ResponseCode;
import com.library.management.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 作用：统一捕获和处理系统中的所有异常，返回规范的错误响应
 *
 * @RestControllerAdvice 注解说明：
 * - 这是一个全局异常处理器，会拦截所有 Controller 抛出的异常
 * - @RestController + @ControllerAdvice 的组合
 * - 自动将返回值序列化为 JSON
 *
 * @ExceptionHandler 注解说明：
 * - 指定要处理的异常类型
 * - 当 Controller 抛出该类型异常时，会被这个方法捕获
 *
 * @ResponseStatus 注解说明：
 * - 设置 HTTP 响应状态码
 * - 虽然业务返回的 Result.code 可能是 400/500，但 HTTP 状态码统一返回 200
 * - 这样前端可以统一根据 Result.code 判断业务是否成功
 *
 * 异常处理流程：
 * 1. Controller 抛出异常
 * 2. 全局异常处理器捕获异常
 * 3. 根据异常类型返回对应的 Result 对象
 * 4. 记录日志
 * 5. 返回 JSON 响应给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 场景：Service 层抛出 BusinessException
     * 例如：用户不存在、数据重复、状态不允许操作
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证/授权异常
     *
     * 场景：用户未登录、Token 过期、权限不足
     * 例如：访问需要认证的接口但未登录
     *
     * @param e 认证异常
     * @return 错误响应
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleAuthException(AuthException e) {
        log.warn("认证/授权异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理权限不足异常
     *
     * 场景：用户已登录，但角色权限不足
     * 例如：普通用户访问管理员接口
     *
     * @param e 权限不足异常
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.fail(ResponseCode.FORBIDDEN, "权限不足，无法访问该资源");
    }

    /**
     * 处理参数校验异常（@Valid/@Validated）
     *
     * 场景：Controller 参数校验失败
     * 例如：@NotNull、@NotBlank、@Size 等注解校验失败
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        // 获取所有字段的错误信息
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败：{}", errorMessage);
        return Result.fail(ResponseCode.VALIDATION_FAILED, "参数校验失败：" + errorMessage);
    }

    /**
     * 处理表单绑定异常
     *
     * 场景：表单数据绑定到对象失败
     * 例如：前端传入的数据类型与后端不匹配
     *
     * @param e 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("表单绑定失败：{}", errorMessage);
        return Result.fail(ResponseCode.BAD_REQUEST, "表单绑定失败：" + errorMessage);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * 场景：URL 路径参数或查询参数类型转换失败
     * 例如：/api/user/{id}，id 应该是数字但传入了字符串
     *
     * @param e 参数类型不匹配异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("参数 '%s' 的值 '%s' 类型不正确，期望类型为 %s",
                e.getName(), e.getValue(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");

        log.warn("参数类型不匹配：{}", errorMessage);
        return Result.fail(ResponseCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理非法参数异常
     *
     * 场景：方法参数不合法
     * 例如：传入 null 值给不允许为 null 的参数
     *
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数：{}", e.getMessage());
        return Result.fail(ResponseCode.BAD_REQUEST, "参数错误：" + e.getMessage());
    }

    /**
     * 处理文件上传大小超限异常
     *
     * 场景：上传的文件大小超过配置的限制
     * 例如：上传超过 50MB 的文件
     *
     * @param e 文件上传大小超限异常
     * @return 错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限：{}", e.getMessage());
        long maxSize = e.getMaxUploadSize();
        String message = maxSize > 0
            ? String.format("上传文件大小超过限制，最大允许 %d MB", maxSize / 1024 / 1024)
            : "上传文件大小超过限制";
        return Result.fail(ResponseCode.BAD_REQUEST, message);
    }

    /**
     * 处理空指针异常
     *
     * 场景：代码中出现 NullPointerException
     * 这通常是代码bug，应该在开发阶段修复
     *
     * @param e 空指针异常
     * @return 错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常：", e);
        return Result.fail(ResponseCode.INTERNAL_SERVER_ERROR, "系统内部错误，请联系管理员");
    }

    /**
     * 处理所有未捕获的异常
     *
     * 场景：兜底处理，捕获所有其他未被处理的异常
     * 这是最后一道防线，防止异常信息直接暴露给用户
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(ResponseCode.INTERNAL_SERVER_ERROR, "系统内部错误，请稍后重试");
    }
}
