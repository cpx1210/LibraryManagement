package com.library.management.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 
 * 使用方式：
 * 在需要记录日志的 Service 方法上添加此注解
 * 
 * 示例：
 * 
 * <pre>
 * {@code @Log(module = "sensitive_word", operationType = "create")}
 * public void addSensitiveWord(SensitiveWordDTO dto) { ... }
 * </pre>
 * 
 * 注意事项：
 * 1. 此注解应标记在 Service 层方法上
 * 2. 方法的返回值会被记录为 newValue
 * 3. 对于 update/delete 操作，需要在方法内先查询原数据
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块
     * 
     * 建议使用英文标识，如：
     * - sensitive_word：敏感词库
     * - problem_book：问题书目库
     * - publisher_whitelist：出版社白名单
     * - purchased_problem_book：已购问题图书
     * - user：用户管理
     * - detection：书单检测
     */
    String module();

    /**
     * 操作类型
     * 
     * 可选值：
     * - create：新增
     * - update：修改
     * - delete：删除
     * - import：导入
     * - export：导出
     */
    String operationType();

    /**
     * 操作描述（可选）
     * 
     * 用于记录更详细的操作说明
     */
    String description() default "";

    /**
     * 是否记录请求参数
     * 
     * 默认为 true，会将方法参数记录到 newValue
     */
    boolean saveRequestData() default true;

    /**
     * 是否记录响应数据
     * 
     * 默认为 true，会将方法返回值记录到 newValue
     */
    boolean saveResponseData() default true;
}
