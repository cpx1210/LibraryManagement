package com.library.management.module.collectionbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.collectionbook.entity.CollectionBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 馆藏图书 Mapper
 */
@Mapper
public interface CollectionBookMapper extends BaseMapper<CollectionBook> {

    @Select("SELECT * FROM collection_books WHERE isbn = #{isbn}")
    List<CollectionBook> selectByIsbn(@Param("isbn") String isbn);

    @Select("SELECT * FROM collection_books WHERE is_problem = #{isProblem}")
    List<CollectionBook> selectByIsProblem(@Param("isProblem") Integer isProblem);

    @Select("SELECT * FROM collection_books WHERE branch_library = #{branchLibrary}")
    List<CollectionBook> selectByBranchLibrary(@Param("branchLibrary") String branchLibrary);

    @Select("SELECT * FROM collection_books WHERE batch = #{batch}")
    List<CollectionBook> selectByBatch(@Param("batch") String batch);

    @Select("SELECT COUNT(*) FROM collection_books WHERE barcode = #{barcode}")
    int countByBarcode(@Param("barcode") String barcode);

    @Select("SELECT COUNT(*) FROM collection_books WHERE is_problem = 0")
    long countNormalBooks();

    @Select("SELECT COUNT(*) FROM collection_books WHERE is_problem = 1")
    long countProblemBooks();

    List<CollectionBook> selectCollectionBooksByConditions(@Param("request") Object request);

    long countCollectionBooksByConditions(@Param("request") Object request);

    List<CollectionBook> selectCollectionBooksBatchByConditions(
            @Param("request") Object request,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize);

    List<CollectionBook> selectNextBatchByProblem(
            @Param("isProblem") Integer isProblem,
            @Param("lastCreateTime") LocalDateTime lastCreateTime,
            @Param("lastBarcode") String lastBarcode,
            @Param("pageSize") int pageSize);

    List<CollectionBook> selectNextBatchByProblemAndBranch(
            @Param("isProblem") Integer isProblem,
            @Param("branchLibrary") String branchLibrary,
            @Param("lastCreateTime") LocalDateTime lastCreateTime,
            @Param("lastBarcode") String lastBarcode,
            @Param("pageSize") int pageSize);

    List<CollectionBook> selectPageByProblemAndBranch(
            @Param("isProblem") Integer isProblem,
            @Param("branchLibrary") String branchLibrary,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);

    long countByProblemAndBranch(
            @Param("isProblem") Integer isProblem,
            @Param("branchLibrary") String branchLibrary);
}
