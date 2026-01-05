package com.library.management.module.detection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.management.common.result.Result;
import com.library.management.module.detection.dto.*;
import com.library.management.module.detection.service.BooklistCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 书单检测 Controller
 */
@Slf4j
@Tag(name = "书单检测管理", description = "书单检测相关接口")
@RestController
@RequestMapping("/booklist-check")
public class BooklistCheckController {

    @Resource
    private BooklistCheckService booklistCheckService;

    /**
     * 上传书单文件并创建检测任务
     */
    @Operation(summary = "上传书单", description = "上传Excel书单文件，创建检测任务并自动开始检测")
    @PostMapping("/upload")
    public Result<BooklistUploadResponse> uploadBooklist(
            @Parameter(description = "Excel文件", required = true) @RequestParam("file") MultipartFile file) {

        // 获取当前用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);

        log.info("用户 {} 上传书单文件", userName);

        BooklistUploadResponse response = booklistCheckService.uploadBooklist(file, userId, userName);

        return Result.success(response);
    }

    /**
     * 从馆藏书目创建检测任务
     */
    @Operation(summary = "从馆藏书目创建检测任务", description = "将馆藏书目数据作为检测任务进行检测")
    @PostMapping("/check-from-collection")
    public Result<BooklistUploadResponse> checkFromCollection(
            @Parameter(description = "查询条件") @RequestBody(required = false) CollectionBookCheckRequest request) {

        // 获取当前用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);

        log.info("用户 {} 从馆藏书目创建检测任务", userName);

        BooklistUploadResponse response = booklistCheckService.checkFromCollection(request, userId, userName);

        return Result.success(response);
    }

    /**
     * 分页查询检测任务列表
     */
    @Operation(summary = "查询检测任务列表", description = "分页查询书单检测任务列表，支持多条件筛选")
    @GetMapping("/tasks")
    public Result<IPage<BooklistCheckTaskDTO>> queryTasks(
            @Parameter(description = "任务名称（模糊查询）") @RequestParam(required = false) String taskName,
            @Parameter(description = "任务状态") @RequestParam(required = false) String status,
            @Parameter(description = "提交人ID") @RequestParam(required = false) Long submittedBy,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        TaskQueryRequest request = new TaskQueryRequest();
        request.setTaskName(taskName);
        request.setStatus(status);
        request.setSubmittedBy(submittedBy);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);

        IPage<BooklistCheckTaskDTO> page = booklistCheckService.queryTasks(request);

        return Result.success(page);
    }

    /**
     * 查询检测任务详情
     */
    @Operation(summary = "查询任务详情", description = "根据任务ID查询检测任务的详细信息")
    @GetMapping("/tasks/{taskId}")
    public Result<BooklistCheckTaskDTO> getTaskDetail(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {

        BooklistCheckTaskDTO task = booklistCheckService.getTaskDetail(taskId);

        return Result.success(task);
    }

    /**
     * 查询检测结果明细列表
     */
    @Operation(summary = "查询检测结果明细", description = "查询指定任务的检测结果明细列表")
    @GetMapping("/tasks/{taskId}/details")
    public Result<List<CheckResultDetailDTO>> getCheckDetails(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "风险等级（可选，用于筛选）") @RequestParam(required = false) String riskLevel) {

        List<CheckResultDetailDTO> details = booklistCheckService.getCheckDetails(taskId, riskLevel);

        return Result.success(details);
    }

    /**
     * 导出检测结果
     */
    @Operation(summary = "导出检测结果", description = "导出检测结果为Excel文件（带颜色标注）")
    @GetMapping("/tasks/{taskId}/export")
    public void exportCheckResult(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            HttpServletResponse response) {

        booklistCheckService.exportCheckResult(taskId, response);
    }

    /**
     * 下载检测模板
     */
    @Operation(summary = "下载检测模板", description = "下载书单检测Excel模板（含示例数据）")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        booklistCheckService.downloadTemplate(response);
    }

    /**
     * 取消检测任务
     */
    @Operation(summary = "取消检测任务", description = "取消正在进行的检测任务")
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {

        booklistCheckService.cancelTask(taskId);

        return Result.success();
    }

    /**
     * 删除检测任务
     */
    @Operation(summary = "删除检测任务", description = "删除指定的检测任务及其检测结果")
    @PostMapping("/tasks/{taskId}/delete")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {

        booklistCheckService.deleteTask(taskId);

        return Result.success();
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return 1L; // 默认用户ID（开发环境）
        }

        // 从 JWT Token 中获取用户ID
        // 实际实现需要根据项目的认证方式调整
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return 1L;
        }
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUserName(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "系统管理员"; // 默认用户名（开发环境）
        }

        // 从 JWT Token 中获取用户名
        // 实际实现需要根据项目的认证方式调整
        return auth.getName();
    }
}