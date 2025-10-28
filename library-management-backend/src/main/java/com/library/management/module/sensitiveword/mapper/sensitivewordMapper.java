package com.library.management.module.sensitiveword.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.sensitiveword.entity.SensitiveWords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 敏感词 Mapper 接口
 *
 * 数据访问层（DAO），负责敏感词表的数据库操作
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法（如根据关键词查询）
 * 3. 支持复杂的 SQL 查询
 *
 * BaseMapper 提供的基础方法：
 * - insert(SensitiveWords word)：插入一条记录
 * - deleteById(Long id)：根据 ID 删除
 * - updateById(SensitiveWords word)：根据 ID 更新
 * - selectById(Long id)：根据 ID 查询
 * - selectList(Wrapper<SensitiveWords> wrapper)：条件查询列表
 * - selectPage(Page<SensitiveWords> page, Wrapper<SensitiveWords> wrapper)：分页查询
 * - 还有更多方法...
 *
 * 注解说明：
 * - @Mapper：MyBatis 注解，标记为 Mapper 接口，Spring 会自动扫描并创建代理对象
 * - @Select：MyBatis 注解，定义查询 SQL
 * - @Param：MyBatis 注解，指定 SQL 中的参数名
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWords> {

    /**
     * 根据关键词查询敏感词（精确匹配）
     *
     * @param keyword 敏感词内容
     * @return 敏感词实体，如果不存在返回 null
     */
    @Select("SELECT * FROM sensitive_words WHERE keyword = #{keyword}")
    SensitiveWords selectByKeyword(@Param("keyword") String keyword);

    /**
     * 检测文本中的敏感词（模糊匹配）
     * 使用 PostgreSQL 的 ILIKE 进行不区分大小写的模糊匹配
     *
     * @param text 待检测的文本（书名或作者名）
     * @return 命中的敏感词列表
     */
    List<SensitiveWords> detectSensitiveWords(@Param("text") String text);
}




























