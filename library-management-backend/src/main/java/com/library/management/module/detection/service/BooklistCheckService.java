package com.library.management.module.detection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.management.module.detection.dto.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 书单检测服务接口
 */
public interface BooklistCheckService {

    /**
     * 上传书单并创建检测任务
     *
     * @param file     Excel文件
     *
     * @param userId   当前用户ID
     * @param userName 当前用户姓名
     * @return 上传响应
     */
    BooklistUploadResponse uploadBooklist(MultipartFile file, Long userId, String userName);

    /**
     * 从馆藏书目创建检测任务
     *
     * @param request  查询条件
     * @param userId   当前用户ID
     * @param userName 当前用户姓名
     * @return 上传响应
     */
    BooklistUploadResponse checkFromCollection(CollectionBookCheckRequest request, Long userId, String userName);

    /**
     * 执行检测任务（异步）
     *
     * @param taskId 任务ID
     */
    void executeDetection(Long taskId);

    /**
     * 分页查询检测任务列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<BooklistCheckTaskDTO> queryTasks(TaskQueryRequest request);

    /**
     * 查询检测任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    BooklistCheckTaskDTO getTaskDetail(Long taskId);

    /**
     * 查询检测结果明细列表
     *
     * @param taskId    任务ID
     * @param riskLevel 风险等级（可选，用于筛选）
     * @return 检测结果明细列表
     */
    List<CheckResultDetailDTO> getCheckDetails(Long taskId, String riskLevel);

    /**
     * 导出检测结果（Excel，带颜色标注）
     *
     * @param taskId   任务ID
     * @param response HTTP响应
     */
    void exportCheckResult(Long taskId, HttpServletResponse response);

    /**
     * 下载检测模板
     *
     * @param response HTTP响应
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 删除检测任务
     *
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);

    /**
     * 取消检测任务
     *
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);
}