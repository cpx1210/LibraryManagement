package com.library.management.module.sensitiveword.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.sensitiveword.entity.SensitiveCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 敏感词分类 Mapper 接口
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法
 *
 * @author Library Management System
 * @since 2025-10-27
 */
@Mapper
public interface SensitiveCategoryMapper extends BaseMapper<SensitiveCategory> {

    /**
     * 根据分类名称查询分类
     *
     * @param categoryName 分类名称
     * @return 敏感词分类实体，如果不存在返回 null
     */
    @Select("SELECT * FROM sensitive_categories WHERE category_name = #{categoryName} LIMIT 1")
    SensitiveCategory selectByCategoryName(@Param("categoryName") String categoryName);
}
