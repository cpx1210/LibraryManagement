package com.library.management.module.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志 DTO（数据传输对象）
 * 
 * 用于返回给前端，包含以下增强信息：
 * - 操作人姓名（通过关联查询获取）
 * - 模块和操作类型的中文描述
 * 
 * @author Library Management System
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO {

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 操作模块（英文标识）
     */
    private String module;

    /**
     * 操作模块名称（中文）
     */
    private String moduleName;

    /**
     * 操作类型（英文标识）
     */
    private String operationType;

    /**
     * 操作类型名称（中文）
     */
    private String operationTypeName;

    /**
     * 目标记录ID
     */
    private Long targetId;

    /**
     * 操作前内容（JSON格式）
     */
    private String oldValue;

    /**
     * 操作后内容（JSON格式）
     */
    private String newValue;

    /**
     * 操作人ID
     */
    private Long operatedBy;

    /**
     * 操作人姓名（关联查询）
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * 获取模块名称（中文）
     * 
     * @param module 模块标识
     * @return 模块中文名称
     */
    public static String getModuleName(String module) {
        if (module == null)
            return "";
        return switch (module) {
            case "sensitive_word" -> "敏感词库";
            case "sensitive_category" -> "敏感词分类";
            case "problem_book" -> "问题书目库";
            case "publisher_whitelist" -> "出版社白名单";
            case "purchased_problem_book" -> "已购问题图书";
            case "user" -> "用户管理";
            case "detection" -> "书单检测";
            case "collection_book" -> "馆藏图书";
            default -> module;
        };
    }

    /**
     * 获取操作类型名称（中文）
     * 
     * @param operationType 操作类型标识
     * @return 操作类型中文名称
     */
    public static String getOperationTypeName(String operationType) {
        if (operationType == null)
            return "";
        return switch (operationType) {
            case "create" -> "新增";
            case "update" -> "修改";
            case "delete" -> "删除";
            case "import" -> "导入";
            case "export" -> "导出";
            case "login" -> "登录";
            case "logout" -> "登出";
            default -> operationType;
        };
    }
}
