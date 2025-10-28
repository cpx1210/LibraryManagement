package com.library.management.module.purchased.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.purchased.entity.PurchasedProblemBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 已购问题图书 Mapper 接口
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法
 *
 * @author Library Management System
 * @since 2025-10-27
 */
@Mapper
public interface PurchasedProblemBookMapper extends BaseMapper<PurchasedProblemBook> {

    /**
     * 根据 ISBN 查询已购问题图书
     *
     * @param isbn ISBN 编号
     * @return 已购问题图书列表
     */
    @Select("SELECT * FROM purchased_problem_books WHERE isbn = #{isbn}")
    List<PurchasedProblemBook> selectByIsbn(@Param("isbn") String isbn);

    /**
     * 根据处理状态查询已购问题图书
     *
     * @param status 处理状态
     * @return 已购问题图书列表
     */
    @Select("SELECT * FROM purchased_problem_books WHERE status = #{status}")
    List<PurchasedProblemBook> selectByStatus(@Param("status") String status);

    /**
     * 根据系统号查询已购问题图书
     *
     * @param systemId 系统号
     * @return 已购问题图书列表
     */
    @Select("SELECT * FROM purchased_problem_books WHERE system_id = #{systemId}")
    List<PurchasedProblemBook> selectBySystemId(@Param("systemId") String systemId);
}
