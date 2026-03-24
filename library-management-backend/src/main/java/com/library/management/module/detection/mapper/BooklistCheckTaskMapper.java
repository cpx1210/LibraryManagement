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

    IPage<BooklistCheckTask> selectTaskPage(
            Page<BooklistCheckTask> page,
            @Param("taskName") String taskName,
            @Param("status") String status,
            @Param("submittedBy") Long submittedBy);

    int countTodayTasksByUser(@Param("userId") Long userId);
}
