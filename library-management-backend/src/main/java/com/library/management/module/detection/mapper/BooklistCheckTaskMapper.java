package com.library.management.module.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.detection.entity.BooklistCheckTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 书单检测任务 Mapper
 */
@Mapper
public interface BooklistCheckTaskMapper extends BaseMapper<BooklistCheckTask> {

    /**
     * 分页查询检测任务列表
     *
     * @param page 分页对象
     * @param taskName 任务名称（模糊查询）
     * @param status 任务状态
     * @param submittedBy 提交人ID
     * @return 分页结果
     */
    IPage<BooklistCheckTask> selectTaskPage(
            Page<BooklistCheckTask> page,
            @Param("taskName") String taskName,
            @Param("status") String status,
            @Param("submittedBy") Long submittedBy
    );

    /**
     * 获取用户今天提交的任务数量（用于生成任务名称）
     *
     * @param userId 用户ID
     * @return 今天提交的任务数量
     */
    int countTodayTasksByUser(@Param("userId") Long userId);
}
