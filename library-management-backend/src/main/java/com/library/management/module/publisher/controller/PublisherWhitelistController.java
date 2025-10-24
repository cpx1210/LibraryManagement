package com.library.management.module.publisher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.result.Result;
import com.library.management.module.publisher.dto.PublisherWhitelistCreateRequest;
import com.library.management.module.publisher.dto.PublisherWhitelistDTO;
import com.library.management.module.publisher.dto.PublisherWhitelistQueryRequest;
import com.library.management.module.publisher.dto.PublisherWhitelistUpdateRequest;
import com.library.management.module.publisher.service.PublisherWhitelistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 出版社白名单管理控制器
 *
 * @Tag: Swagger 文档标签
 * @RestController: 组合注解，等同于 @Controller + @ResponseBody
 * @RequestMapping: 定义基础路径为 /publisher-whitelist
 * @RequiredArgsConstructor: Lombok 注解，自动注入 final 字段
 *
 * RESTful API 设计规范：
 * - GET /publisher-whitelist：查询列表
 * - GET /publisher-whitelist/{publisherId}：查询详情
 * - POST /publisher-whitelist：创建新记录
 * - PUT /publisher-whitelist：更新记录
 * - DELETE /publisher-whitelist/{publisherId}：删除记录
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Tag(name = "出版社白名单管理", description = "出版社白名单增删改查接口")
@RestController
@RequestMapping("/publisher-whitelist")
@RequiredArgsConstructor
public class PublisherWhitelistController {

    private final PublisherWhitelistService publisherWhitelistService;

    /**
     * 分页查询出版社白名单列表（支持多条件查询）
     *
     * @param request 查询条件（自动绑定查询参数）
     * @return 分页结果
     *
     * 支持的查询条件：
     * - publisherName：出版社名称（模糊查询）
     * - years：年份批次（精确查询）
     * - isActive：是否启用（精确查询）
     * - pageNum：页码（默认第1页）
     * - pageSize：每页大小（默认10条）
     */
    @Operation(summary = "查询出版社白名单列表", description = "支持分页和多条件查询（出版社名称、年份批次、是否启用）")
    @GetMapping
    public Result<Page<PublisherWhitelistDTO>> queryPublishers(PublisherWhitelistQueryRequest request) {
        Page<PublisherWhitelistDTO> page = publisherWhitelistService.queryPublishers(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询出版社白名单详情
     *
     * @param publisherId 出版社ID（路径参数）
     * @return 出版社白名单信息
     */
    @Operation(summary = "查询出版社白名单详情", description = "根据出版社ID获取详细信息")
    @GetMapping("/{publisherId}")
    public Result<PublisherWhitelistDTO> getPublisherById(@PathVariable Long publisherId) {
        PublisherWhitelistDTO publisher = publisherWhitelistService.getPublisherById(publisherId);
        return Result.success(publisher);
    }

    /**
     * 创建新出版社白名单记录
     *
     * @param request 出版社白名单创建请求（JSON 请求体）
     * @return 创建的出版社白名单信息
     *
     * @Valid: 启用 Bean Validation 参数校验
     * @RequestBody: 接收 JSON 格式的请求体
     *
     * 请求体示例：
     * {
     *   "publisherName": "人民教育出版社",
     *   "years": 2024,
     *   "isActive": true
     * }
     */
    @Operation(summary = "创建新出版社白名单", description = "新增出版社白名单记录")
    @PostMapping
    public Result<PublisherWhitelistDTO> createPublisher(@Valid @RequestBody PublisherWhitelistCreateRequest request) {
        // TODO: 获取当前登录用户ID（暂时使用固定值 1L）
        // 实际应从 SecurityContext 或 JWT Token 中获取当前用户ID
        Long currentUserId = 1L;
        PublisherWhitelistDTO publisher = publisherWhitelistService.createPublisher(request, currentUserId);
        return Result.success("出版社白名单创建成功", publisher);
    }

    /**
     * 修改出版社白名单信息
     *
     * @param request 出版社白名单修改请求（JSON 请求体）
     * @return 修改后的出版社白名单信息
     *
     * 请求体示例：
     * {
     *   "publisherId": 1,
     *   "publisherName": "修改后的出版社名称",
     *   "years": 2025,
     *   "isActive": false
     * }
     *
     * 说明：
     * - publisherId 必填，用于指定要修改的出版社白名单记录
     * - 其他字段可选，只更新提供的字段
     */
    @Operation(summary = "修改出版社白名单信息", description = "更新出版社白名单信息（部分更新）")
    @PutMapping
    public Result<PublisherWhitelistDTO> updatePublisher(@Valid @RequestBody PublisherWhitelistUpdateRequest request) {
        PublisherWhitelistDTO publisher = publisherWhitelistService.updatePublisher(request);
        return Result.success("出版社白名单修改成功", publisher);
    }

    /**
     * 删除出版社白名单记录
     *
     * @param publisherId 出版社ID（路径参数）
     * @return 删除结果
     */
    @Operation(summary = "删除出版社白名单", description = "物理删除出版社白名单记录（不可恢复）")
    @DeleteMapping("/{publisherId}")
    public Result<Void> deletePublisher(@PathVariable Long publisherId) {
        publisherWhitelistService.deletePublisher(publisherId);
        return Result.success("出版社白名单删除成功", null);
    }

    /**
     * 批量导入出版社白名单
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果统计
     */
    @Operation(summary = "批量导入出版社白名单", description = "通过 Excel 文件批量导入出版社白名单")
    @PostMapping("/import")
    public Result<Map<String, Object>> importPublishers(@RequestParam("file") MultipartFile file) {
        // TODO: 获取当前登录用户ID（暂时使用固定值 1L）
        Long currentUserId = 1L;
        Map<String, Object> result = publisherWhitelistService.importPublishers(file, currentUserId);
        return Result.success("导入完成", result);
    }

    /**
     * 批量导出出版社白名单
     *
     * @param response HTTP 响应对象
     * @param request 查询条件（可选）
     */
    @Operation(summary = "批量导出出版社白名单", description = "导出出版社白名单为 Excel 文件")
    @GetMapping("/export")
    public void exportPublishers(HttpServletResponse response, PublisherWhitelistQueryRequest request) {
        publisherWhitelistService.exportPublishers(response, request);
    }

    /**
     * 下载出版社白名单导入模板
     *
     * @param response HTTP 响应对象
     */
    @Operation(summary = "下载导入模板", description = "下载出版社白名单导入模板文件")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        publisherWhitelistService.downloadTemplate(response);
    }

    /**
     * 获取所有启用的出版社白名单（用于书单检测等场景）
     *
     * @return 所有启用的出版社白名单列表
     */
    @Operation(summary = "获取所有启用的出版社白名单", description = "获取完整的启用状态的出版社白名单列表（不分页）")
    @GetMapping("/all")
    public Result<List<PublisherWhitelistDTO>> getAllActivePublishers() {
        List<PublisherWhitelistDTO> publishers = publisherWhitelistService.getAllActivePublishers();
        return Result.success(publishers);
    }
}
