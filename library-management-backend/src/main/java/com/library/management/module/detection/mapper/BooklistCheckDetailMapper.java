package com.library.management.module.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.detection.entity.BooklistCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 书单检测结果明细 Mapper
 */
@Mapper
public interface BooklistCheckDetailMapper extends BaseMapper<BooklistCheckDetail> {

    int batchInsert(@Param("details") List<BooklistCheckDetail> details);

    List<BooklistCheckDetail> selectByTaskId(@Param("taskId") Long taskId);

    List<BooklistCheckDetail> selectTaskBatch(
            @Param("taskId") Long taskId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize);

    IPage<BooklistCheckDetail> selectDetailPage(
            Page<BooklistCheckDetail> page,
            @Param("taskId") Long taskId,
            @Param("riskLevel") String riskLevel,
            @Param("hasIssue") Boolean hasIssue);

    List<BooklistCheckDetail> selectProblemBooksByTaskId(@Param("taskId") Long taskId);

    Map<String, Integer> countResultByTaskId(@Param("taskId") Long taskId);
}
