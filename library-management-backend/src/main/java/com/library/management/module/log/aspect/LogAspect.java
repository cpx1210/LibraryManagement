package com.library.management.module.log.aspect;

import cn.hutool.json.JSONUtil;
import com.library.management.common.annotation.Log;
import com.library.management.module.log.entity.OperationLog;
import com.library.management.module.log.service.OperationLogService;
import com.library.management.module.user.entity.SysUser;
import com.library.management.module.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 
 * 核心功能：
 * 1. 拦截带有 @Log 注解的方法
 * 2. 自动记录操作日志（操作人、操作时间、IP地址等）
 * 3. 支持记录方法参数和返回值
 * 4. 异步保存日志，不阻塞主业务
 * 
 * AOP 切面工作流程：
 * 1. @Before：方法执行前（可用于获取原始数据）
 * 2. @AfterReturning：方法正常返回后（记录成功日志）
 * 3. @AfterThrowing：方法抛出异常后（记录失败日志）
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogService operationLogService;
    private final UserMapper userMapper;

    /**
     * ThreadLocal 存储原始数据（用于 update/delete 操作）
     * 
     * 说明：在方法执行前，可以将原始数据存入此变量
     * 方法执行后，从此变量获取原始数据记录到日志
     */
    private static final ThreadLocal<String> OLD_VALUE_HOLDER = new ThreadLocal<>();

    /**
     * 定义切点：所有带有 @Log 注解的方法
     */
    @Pointcut("@annotation(com.library.management.common.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 方法执行前：清空 ThreadLocal
     * 
     * 说明：确保每次请求的 ThreadLocal 是干净的
     */
    @Before("logPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        OLD_VALUE_HOLDER.remove();
    }

    /**
     * 方法正常返回后：记录操作日志
     * 
     * @param joinPoint 切点
     * @param result    方法返回值
     */
    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            handleLog(joinPoint, null, result);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        } finally {
            OLD_VALUE_HOLDER.remove();
        }
    }

    /**
     * 方法抛出异常后：记录错误日志
     * 
     * @param joinPoint 切点
     * @param exception 异常对象
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, Exception exception) {
        try {
            handleLog(joinPoint, exception, null);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        } finally {
            OLD_VALUE_HOLDER.remove();
        }
    }

    /**
     * 处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Exception exception, Object result) {
        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        if (logAnnotation == null) {
            return;
        }

        // 2. 构建日志实体
        OperationLog operationLog = OperationLog.builder()
                .module(logAnnotation.module())
                .operationType(logAnnotation.operationType())
                .operationTime(LocalDateTime.now())
                .build();

        // 3. 获取当前登录用户
        Long currentUserId = getCurrentUserId();
        operationLog.setOperatedBy(currentUserId);

        // 4. 获取客户端 IP
        String ipAddress = getClientIp();
        operationLog.setIpAddress(ipAddress);

        // 5. 记录请求参数
        if (logAnnotation.saveRequestData()) {
            String requestData = getRequestData(joinPoint);
            // 对于 create 操作，请求参数作为 newValue
            if ("create".equals(logAnnotation.operationType())) {
                operationLog.setNewValue(requestData);
            }
        }

        // 6. 记录响应数据
        if (logAnnotation.saveResponseData() && result != null) {
            String responseData = toJsonString(result);
            // 更新 newValue（如果返回值中包含 ID，优先使用返回值）
            if (result != null) {
                operationLog.setNewValue(responseData);
                // 尝试从返回值中提取目标ID
                Long targetId = extractTargetId(result);
                if (targetId != null) {
                    operationLog.setTargetId(targetId);
                }
            }
        }

        // 7. 获取原始数据（如果有）
        String oldValue = OLD_VALUE_HOLDER.get();
        if (oldValue != null) {
            operationLog.setOldValue(oldValue);
        }

        // 8. 如果发生异常，记录异常信息
        if (exception != null) {
            operationLog.setNewValue("操作失败: " + exception.getMessage());
        }

        // 9. 异步保存日志
        operationLogService.saveLogAsync(operationLog);

        log.debug("操作日志已记录: module={}, type={}, operator={}",
                operationLog.getModule(), operationLog.getOperationType(), operationLog.getOperatedBy());
    }

    /**
     * 获取当前登录用户ID
     * 
     * 说明：从 SecurityContext 获取当前登录用户名，然后查询用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                String username = null;

                // 提取用户名
                if (principal instanceof Long) {
                    return (Long) principal;
                } else if (principal instanceof String) {
                    username = (String) principal;
                } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                }

                // 通过用户名查询用户ID
                if (username != null && !"anonymousUser".equals(username)) {
                    SysUser user = userMapper.selectByUsername(username);
                    if (user != null) {
                        return user.getUserId();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 优先从 X-Forwarded-For 头获取（代理环境）
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }

                // 如果是多个代理，取第一个IP
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }

                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
        }
        return "unknown";
    }

    /**
     * 获取请求参数
     */
    private String getRequestData(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉 HttpServletRequest、HttpServletResponse 等特殊参数
                StringBuilder sb = new StringBuilder();
                for (Object arg : args) {
                    if (arg != null && !isFilteredType(arg)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(toJsonString(arg));
                    }
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 判断是否需要过滤的类型
     */
    private boolean isFilteredType(Object obj) {
        return obj instanceof HttpServletRequest
                || obj instanceof jakarta.servlet.http.HttpServletResponse
                || obj instanceof org.springframework.web.multipart.MultipartFile;
    }

    /**
     * 对象转 JSON 字符串
     */
    private String toJsonString(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return JSONUtil.toJsonStr(obj);
        } catch (Exception e) {
            log.warn("对象转JSON失败: {}", e.getMessage());
            return obj.toString();
        }
    }

    /**
     * 从返回值中提取目标ID
     * 
     * 说明：尝试从返回对象中获取 ID 字段
     */
    private Long extractTargetId(Object result) {
        try {
            if (result == null) {
                return null;
            }

            // 尝试获取常见的 ID 字段
            Class<?> clazz = result.getClass();

            // 尝试 getId 方法
            try {
                java.lang.reflect.Method getIdMethod = clazz.getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (NoSuchMethodException ignored) {
            }

            // 尝试其他 ID 命名方式
            String[] idFieldNames = { "userId", "wordId", "bookId", "publisherId", "logId", "taskId" };
            for (String fieldName : idFieldNames) {
                try {
                    String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    java.lang.reflect.Method method = clazz.getMethod(methodName);
                    Object id = method.invoke(result);
                    if (id instanceof Long) {
                        return (Long) id;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (Exception e) {
            log.debug("提取目标ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 设置原始数据（供业务代码调用）
     * 
     * 使用方式：在 Service 方法中，执行更新/删除操作前调用此方法
     * 
     * <pre>
     * LogAspect.setOldValue(JSONUtil.toJsonStr(originalEntity));
     * </pre>
     * 
     * @param oldValue 原始数据的 JSON 字符串
     */
    public static void setOldValue(String oldValue) {
        OLD_VALUE_HOLDER.set(oldValue);
    }

    /**
     * 获取原始数据
     */
    public static String getOldValue() {
        return OLD_VALUE_HOLDER.get();
    }

    /**
     * 清除原始数据
     */
    public static void clearOldValue() {
        OLD_VALUE_HOLDER.remove();
    }
}
