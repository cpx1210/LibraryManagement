package com.library.management.module.detection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.management.common.exception.BusinessException;
import com.library.management.common.result.Result;
import com.library.management.module.detection.dto.BooklistCheckTaskDTO;
import com.library.management.module.detection.dto.BooklistUploadResponse;
import com.library.management.module.detection.dto.BooklistUploadSubmitterDTO;
import com.library.management.module.detection.dto.CheckResultDetailDTO;
import com.library.management.module.detection.dto.CollectionBookCheckRequest;
import com.library.management.module.detection.dto.TaskQueryRequest;
import com.library.management.module.detection.service.BooklistCheckService;
import com.library.management.module.detection.service.impl.CollectionBookDetectionService;
import com.library.management.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @Resource
    private CollectionBookDetectionService collectionBookDetectionService;

    @Resource
    private UserService userService;

    /**
     * 上传书单文件并创建检测任务
     */
    @Operation(summary = "上传书单", description = "上传 Excel 书单文件，创建检测任务并自动开始检测")
    @PostMapping("/upload")
    public Result<BooklistUploadResponse> uploadBooklist(
            @Parameter(description = "Excel 文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "上传人姓名") @RequestParam(required = false) String submitterName,
            @Parameter(description = "部门") @RequestParam(required = false) String department,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "工号") @RequestParam(required = false) String employeeNo,
            @Parameter(description = "手机号") @RequestParam(required = false) String mobile) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);
        BooklistUploadSubmitterDTO submitterInfo = BooklistUploadSubmitterDTO.builder()
                .submitterName(submitterName)
                .department(department)
                .email(email)
                .employeeNo(employeeNo)
                .mobile(mobile)
                .build();

        log.info("用户 {} 上传书单文件", userName);

        BooklistUploadResponse response = booklistCheckService.uploadBooklist(file, userId, userName, submitterInfo);
        return Result.success(response);
    }

    /**
     * 从馆藏书目创建检测任务
     */
    @Operation(summary = "从馆藏书目创建检测任务", description = "将馆藏书目数据作为检测任务进行检测")
    @PostMapping("/check-from-collection")
    public Result<BooklistUploadResponse> checkFromCollection(
            @Parameter(description = "查询条件") @RequestBody(required = false) CollectionBookCheckRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);

        log.info("用户 {} 从馆藏书目创建检测任务", userName);

        BooklistUploadResponse response = collectionBookDetectionService.createTask(request, userId, userName);
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
            @Parameter(description = "提交人 ID") @RequestParam(required = false) Long submittedBy,
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
    @Operation(summary = "查询任务详情", description = "根据任务 ID 查询检测任务的详细信息")
    @GetMapping("/tasks/{taskId}")
    public Result<BooklistCheckTaskDTO> getTaskDetail(
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId) {

        BooklistCheckTaskDTO task = booklistCheckService.getTaskDetail(taskId);
        return Result.success(task);
    }

    /**
     * 查询检测结果明细列表
     */
    @Operation(summary = "查询检测结果明细", description = "查询指定任务的检测结果明细列表")
    @GetMapping("/tasks/{taskId}/details")
    public Result<List<CheckResultDetailDTO>> getCheckDetails(
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "风险等级（可选，用于筛选）") @RequestParam(required = false) String riskLevel) {

        List<CheckResultDetailDTO> details = booklistCheckService.getCheckDetails(taskId, riskLevel);
        return Result.success(details);
    }

    /**
     * 分页查询检测结果明细列表
     */
    @Operation(summary = "分页查询检测结果明细", description = "分页查询指定任务的检测结果明细列表，避免一次性返回大量数据")
    @GetMapping("/tasks/{taskId}/details/page")
    public Result<IPage<CheckResultDetailDTO>> getCheckDetailsPage(
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "风险等级（可选，用于筛选）") @RequestParam(required = false) String riskLevel,
            @Parameter(description = "是否只看问题数据") @RequestParam(defaultValue = "true") Boolean hasIssue,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "50") Integer pageSize) {

        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null ? 50 : Math.min(Math.max(pageSize, 1), 200);

        IPage<CheckResultDetailDTO> page = booklistCheckService.getCheckDetailsPage(
                taskId,
                riskLevel,
                hasIssue,
                safePageNum,
                safePageSize);

        return Result.success(page);
    }

    /**
     * 导出检测结果
     */
    @Operation(summary = "创建导出任务", description = "后台生成检测结果 Excel 文件，前端可轮询进度并在完成后下载")
    @PostMapping("/tasks/{taskId}/export")
    public Result<BooklistCheckTaskDTO> startExportCheckResult(
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId) {

        BooklistCheckTaskDTO task = booklistCheckService.startExportCheckResult(taskId);
        return Result.success(task);
    }

    /**
     * 下载检测结果
     */
    @Operation(summary = "下载检测结果", description = "下载后台已生成的检测结果 Excel 文件")
    @GetMapping("/tasks/{taskId}/export")
    public void downloadExportCheckResult(
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId,
            HttpServletResponse response) {

        booklistCheckService.downloadExportCheckResult(taskId, response);
    }

    /**
     * 下载检测模板
     */
    @Operation(summary = "下载检测模板", description = "下载书单检测 Excel 模板（含示例数据）")
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
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId) {

        booklistCheckService.cancelTask(taskId);
        return Result.success();
    }

    /**
     * 删除检测任务
     */
    @Operation(summary = "删除检测任务", description = "删除指定的检测任务及其检测结果")
    @PostMapping("/tasks/{taskId}/delete")
    public Result<Void> deleteTask(
            @Parameter(description = "任务 ID", required = true) @PathVariable Long taskId) {

        booklistCheckService.deleteTask(taskId);
        return Result.success();
    }

    private Long getCurrentUserId(Authentication auth) {
        if (isAnonymous(auth)) {
            return 1L;
        }

        String username = auth.getName();
        Long userId = userService.getUserIdByUsername(username);
        if (userId == null) {
            throw new BusinessException("当前登录用户不存在或已失效");
        }
        return userId;
    }

    private String getCurrentUserName(Authentication auth) {
        if (isAnonymous(auth)) {
            return "公共上传";
        }

        return auth.getName();
    }

    private boolean isAnonymous(Authentication auth) {
        return auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName());
    }
}
