package com.library.management.module.publisher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.publisher.entity.PublisherWhitelist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 出版社白名单 Mapper 接口
 *
 * 数据访问层（DAO），负责出版社白名单表的数据库操作
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法（如根据出版社名称查询）
 * 3. 支持复杂的 SQL 查询
 *
 * BaseMapper 提供的基础方法：
 * - insert(PublisherWhitelist publisher)：插入一条记录
 * - deleteById(Long id)：根据 ID 删除
 * - updateById(PublisherWhitelist publisher)：根据 ID 更新
 * - selectById(Long id)：根据 ID 查询
 * - selectList(Wrapper<PublisherWhitelist> wrapper)：条件查询列表
 * - selectPage(Page<PublisherWhitelist> page, Wrapper<PublisherWhitelist> wrapper)：分页查询
 * - 还有更多方法...
 *
 * 注解说明：
 * - @Mapper：MyBatis 注解，标记为 Mapper 接口，Spring 会自动扫描并创建代理对象
 * - @Select：MyBatis 注解，定义查询 SQL
 * - @Param：MyBatis 注解，指定 SQL 中的参数名
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Mapper
public interface PublisherWhitelistMapper extends BaseMapper<PublisherWhitelist> {

    /**
     * 根据出版社名称查询白名单记录（精确匹配）
     *
     * @param publisherName 出版社名称
     * @return 出版社白名单实体，如果不存在返回 null
     */
    @Select("SELECT * FROM publisher_whitelist WHERE publisher_name = #{publisherName} LIMIT 1")
    PublisherWhitelist selectByPublisherName(@Param("publisherName") String publisherName);

    /**
     * 根据出版社名称查询启用状态的白名单记录（精确匹配）
     *
     * @param publisherName 出版社名称
     * @return 启用状态的出版社白名单实体，如果不存在返回 null
     */
    @Select("SELECT * FROM publisher_whitelist WHERE publisher_name = #{publisherName} AND is_active = true LIMIT 1")
    PublisherWhitelist selectActiveByPublisherName(@Param("publisherName") String publisherName);
}
