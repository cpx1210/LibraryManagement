package com.library.management.module.problembook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.problembook.entity.ProblemBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 问题书目 Mapper 接口
 *
 * 数据访问层（DAO），负责问题书目表的数据库操作
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法（如根据 ISBN、书名查询）
 * 3. 支持复杂的 SQL 查询
 *
 * BaseMapper 提供的基础方法：
 * - insert(ProblemBook book)：插入一条记录
 * - deleteById(Long id)：根据 ID 删除
 * - updateById(ProblemBook book)：根据 ID 更新
 * - selectById(Long id)：根据 ID 查询
 * - selectList(Wrapper<ProblemBook> wrapper)：条件查询列表
 * - selectPage(Page<ProblemBook> page, Wrapper<ProblemBook> wrapper)：分页查询
 * - 还有更多方法...
 *
 * 注解说明：
 * - @Mapper：MyBatis 注解，标记为 Mapper 接口，Spring 会自动扫描并创建代理对象
 * - @Select：MyBatis 注解，定义查询 SQL
 * - @Param：MyBatis 注解，指定 SQL 中的参数名
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Mapper
public interface ProblemBookMapper extends BaseMapper<ProblemBook> {

    /**
     * 根据 ISBN 查询问题书目（精确匹配）
     *
     * @param isbn ISBN 编号
     * @return 问题书目实体，如果不存在返回 null
     */
    @Select("SELECT * FROM problem_books WHERE isbn = #{isbn} LIMIT 1")
    ProblemBook selectByIsbn(@Param("isbn") String isbn);

    /**
     * 根据书名查询问题书目（精确匹配）
     *
     * @param bookName 书名
     * @return 问题书目实体，如果不存在返回 null
     */
    @Select("SELECT * FROM problem_books WHERE book_name = #{bookName} LIMIT 1")
    ProblemBook selectByBookName(@Param("bookName") String bookName);
}
