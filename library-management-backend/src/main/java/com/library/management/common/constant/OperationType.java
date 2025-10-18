package com.library.management.common.constant;

/**
 * 操作类型常量类
 *
 * 作用：定义系统中所有的操作类型，用于日志记录
 *
 * 使用场景：
 * 1. @Log 注解的 operationType 属性值
 * 2. 操作日志表（operation_log）的 operation_type 字段值
 * 3. 日志查询时的筛选条件
 *
 * 对应数据库约束：
 * operation_type VARCHAR(20) CHECK (operation_type IN ('create', 'update', 'delete', 'query', 'export', 'import'))
 */
public class OperationType {

    /**
     * 新增操作
     * 例如：新增用户、新增敏感词、新增问题书目
     */
    public static final String CREATE = "create";

    /**
     * 修改操作
     * 例如：修改用户信息、修改敏感词、修改问题书目
     */
    public static final String UPDATE = "update";

    /**
     * 删除操作
     * 例如：删除用户、删除敏感词、删除问题书目
     */
    public static final String DELETE = "delete";

    /**
     * 查询操作
     * 例如：查询用户列表、查询敏感词、查询检测结果
     */
    public static final String QUERY = "query";

    /**
     * 导出操作
     * 例如：导出用户列表、导出敏感词库、导出检测结果
     */
    public static final String EXPORT = "export";

    /**
     * 导入操作
     * 例如：批量导入用户、批量导入敏感词、批量导入问题书目
     */
    public static final String IMPORT = "import";

    /**
     * 私有构造函数，防止实例化
     */
    private OperationType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
