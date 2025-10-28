package com.library.management.module.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.management.module.detection.entity.BooklistCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 书单检测结果明细 Mapper
 */
@Mapper
public interface BooklistCheckDetailMapper extends BaseMapper<BooklistCheckDetail> {

    /**
     * 批量插入检测结果明细
     *
     * @param details 检测结果明细列表
     * @return 插入的行数
     */
    int batchInsert(@Param("details") List<BooklistCheckDetail> details);

    /**
     * 根据任务ID查询检测结果明细
     *
     * @param taskId 任务ID
     * @return 检测结果明细列表
     */
    List<BooklistCheckDetail> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据任务ID查询有问题的书目（命中敏感词或问题书目）
     *
     * @param taskId 任务ID
     * @return 有问题的书目列表
     */
    List<BooklistCheckDetail> selectProblemBooksByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计任务的检测结果
     *
     * @param taskId 任务ID
     * @return 统计结果（Map：sensitive_hits, problem_book_hits, non_whitelist_pubs）
     */
    java.util.Map<String, Integer> countResultByTaskId(@Param("taskId") Long taskId);
}
