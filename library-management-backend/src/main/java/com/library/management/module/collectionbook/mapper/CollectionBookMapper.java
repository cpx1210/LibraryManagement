package com.library.management.module.collectionbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.collectionbook.entity.CollectionBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 馆藏图书 Mapper 接口
 *
 * 功能说明：
 * 1. 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法
 * 2. 定义自定义查询方法
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Mapper
public interface CollectionBookMapper extends BaseMapper<CollectionBook> {

    /**
     * 根据 ISBN 查询馆藏图书
     *
     * @param isbn ISBN 编号
     * @return 馆藏图书列表
     */
    @Select("SELECT * FROM collection_books WHERE isbn = #{isbn}")
    List<CollectionBook> selectByIsbn(@Param("isbn") String isbn);

    /**
     * 根据是否问题图书查询
     *
     * @param isProblem 是否问题图书（0-正常，1-问题）
     * @return 馆藏图书列表
     */
    @Select("SELECT * FROM collection_books WHERE is_problem = #{isProblem}")
    List<CollectionBook> selectByIsProblem(@Param("isProblem") Integer isProblem);

    /**
     * 根据分馆查询馆藏图书
     *
     * @param branchLibrary 分馆名称
     * @return 馆藏图书列表
     */
    @Select("SELECT * FROM collection_books WHERE branch_library = #{branchLibrary}")
    List<CollectionBook> selectByBranchLibrary(@Param("branchLibrary") String branchLibrary);

    /**
     * 根据批次查询馆藏图书
     *
     * @param batch 批次
     * @return 馆藏图书列表
     */
    @Select("SELECT * FROM collection_books WHERE batch = #{batch}")
    List<CollectionBook> selectByBatch(@Param("batch") String batch);

    /**
     * 检查条码是否存在
     *
     * @param barcode 条码
     * @return 数量（0表示不存在，>0表示存在）
     */
    @Select("SELECT COUNT(*) FROM collection_books WHERE barcode = #{barcode}")
    int countByBarcode(@Param("barcode") String barcode);

    /**
     * 统计正常馆藏图书数量
     *
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM collection_books WHERE is_problem = 0")
    long countNormalBooks();

    /**
     * 统计问题图书数量
     *
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM collection_books WHERE is_problem = 1")
    long countProblemBooks();

    /**
     * 根据条件查询馆藏书目（用于检测）
     *
     * @param request 查询条件
     * @return 馆藏图书列表
     */
    List<CollectionBook> selectCollectionBooksByConditions(@Param("request") Object request);
}
