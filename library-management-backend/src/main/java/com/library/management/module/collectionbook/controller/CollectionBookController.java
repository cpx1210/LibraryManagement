package com.library.management.module.collectionbook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.result.Result;
import com.library.management.module.collectionbook.dto.*;
import com.library.management.module.collectionbook.service.CollectionBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 馆藏图书管理控制器
 *
 * 提供馆藏图书的增删改查、导入导出等功能
 * 通过 is_problem 参数区分"馆藏图书"和"馆藏问题图书"
 *
 * RESTful API 设计：
 * - GET /collection-books：查询列表
 * - GET /collection-books/{barcode}：查询详情
 * - POST /collection-books：创建新记录
 * - PUT /collection-books：更新记录
 * - DELETE /collection-books/{barcode}：删除记录
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Tag(name = "馆藏图书管理", description = "馆藏图书增删改查、导入导出接口")
@RestController
@RequestMapping("/collection-books")
@RequiredArgsConstructor
public class CollectionBookController {

    private final CollectionBookService collectionBookService;

    /**
     * 分页查询馆藏图书列表（支持多条件查询）
     *
     * @param request 查询条件
     * @return 分页结果
     *
     *         说明：
     *         - 查询馆藏图书时，设置 isProblem=0
     *         - 查询问题图书时，设置 isProblem=1
     *         - 不设置 isProblem 则查询所有
     */
    @Operation(summary = "查询馆藏图书列表", description = "支持分页和多条件查询（条码、题名、著者、ISBN、出版社等）")
    @GetMapping
    public Result<Page<CollectionBookDTO>> queryBooks(CollectionBookQueryRequest request) {
        Page<CollectionBookDTO> page = collectionBookService.queryBooks(request);
        return Result.success(page);
    }

    /**
     * 根据条码查询馆藏图书详情
     *
     * @param barcode 条码
     * @return 馆藏图书信息
     */
    @Operation(summary = "查询馆藏图书详情", description = "根据条码获取详细信息")
    @GetMapping("/{barcode}")
    public Result<CollectionBookDTO> getBookByBarcode(
            @Parameter(description = "条码") @PathVariable String barcode) {
        CollectionBookDTO book = collectionBookService.getBookByBarcode(barcode);
        return Result.success(book);
    }

    /**
     * 创建新馆藏图书
     *
     * @param request 创建请求
     * @return 创建的馆藏图书信息
     */
    @Operation(summary = "创建新馆藏图书", description = "新增馆藏图书")
    @PostMapping
    public Result<CollectionBookDTO> createBook(@Valid @RequestBody CollectionBookCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        CollectionBookDTO book = collectionBookService.createBook(request, currentUserId);
        return Result.success("馆藏图书创建成功", book);
    }

    /**
     * 修改馆藏图书信息
     *
     * @param request 修改请求
     * @return 修改后的馆藏图书信息
     */
    @Operation(summary = "修改馆藏图书信息", description = "更新馆藏图书信息（部分更新）")
    @PutMapping
    public Result<CollectionBookDTO> updateBook(@Valid @RequestBody CollectionBookUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        CollectionBookDTO book = collectionBookService.updateBook(request, currentUserId);
        return Result.success("馆藏图书修改成功", book);
    }

    /**
     * 删除馆藏图书
     *
     * @param barcode 条码
     * @return 删除结果
     */
    @Operation(summary = "删除馆藏图书", description = "物理删除馆藏图书（不可恢复）")
    @DeleteMapping("/{barcode}")
    public Result<Void> deleteBook(
            @Parameter(description = "条码") @PathVariable String barcode) {
        collectionBookService.deleteBook(barcode);
        return Result.success("馆藏图书删除成功", null);
    }

    /**
     * 批量删除馆藏图书
     *
     * @param barcodes 条码列表
     * @return 删除数量
     */
    @Operation(summary = "批量删除馆藏图书", description = "批量物理删除馆藏图书")
    @DeleteMapping("/batch")
    public Result<Integer> deleteBatchBooks(@RequestBody List<String> barcodes) {
        int deleted = collectionBookService.deleteBatchBooks(barcodes);
        return Result.success("批量删除成功，删除数量：" + deleted, deleted);
    }

    /**
     * 批量导入馆藏图书
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果统计
     */
    @Operation(summary = "批量导入馆藏图书", description = "通过 Excel 文件批量导入馆藏图书")
    @PostMapping("/import")
    public Result<Map<String, Object>> importBooks(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        Long currentUserId = getCurrentUserId();
        Map<String, Object> result = collectionBookService.importBooks(file, currentUserId);
        return Result.success("导入完成", result);
    }

    /**
     * 批量导出馆藏图书
     *
     * @param response HTTP 响应对象
     * @param request  查询条件（可选）
     */
    @Operation(summary = "批量导出馆藏图书", description = "导出馆藏图书为 Excel 文件")
    @GetMapping("/export")
    public void exportBooks(HttpServletResponse response, CollectionBookQueryRequest request) {
        collectionBookService.exportBooks(response, request);
    }

    /**
     * 下载馆藏图书导入模板
     *
     * @param response HTTP 响应对象
     */
    @Operation(summary = "下载导入模板", description = "下载馆藏图书导入模板文件")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        collectionBookService.downloadTemplate(response);
    }

    /**
     * 获取所有正常馆藏图书（不分页）
     *
     * @return 正常馆藏图书列表
     */
    @Operation(summary = "获取所有正常馆藏图书", description = "获取完整的正常馆藏图书列表（不分页）")
    @GetMapping("/all/normal")
    public Result<List<CollectionBookDTO>> getAllNormalBooks() {
        List<CollectionBookDTO> books = collectionBookService.getAllNormalBooks();
        return Result.success(books);
    }

    /**
     * 获取所有问题图书（不分页）
     *
     * @return 问题图书列表
     */
    @Operation(summary = "获取所有问题图书", description = "获取完整的问题图书列表（不分页）")
    @GetMapping("/all/problem")
    public Result<List<CollectionBookDTO>> getAllProblemBooks() {
        List<CollectionBookDTO> books = collectionBookService.getAllProblemBooks();
        return Result.success(books);
    }

    /**
     * 将馆藏图书标记为问题图书
     *
     * @param barcode       条码
     * @param problemType   问题类型
     * @param problemReason 问题原因
     * @return 更新后的馆藏图书信息
     */
    @Operation(summary = "标记为问题图书", description = "将馆藏图书标记为问题图书")
    @PostMapping("/{barcode}/mark-problem")
    public Result<CollectionBookDTO> markAsProblem(
            @Parameter(description = "条码") @PathVariable String barcode,
            @Parameter(description = "问题类型") @RequestParam(required = false) String problemType,
            @Parameter(description = "问题原因") @RequestParam(required = false) String problemReason) {
        Long currentUserId = getCurrentUserId();
        CollectionBookDTO book = collectionBookService.markAsProblem(barcode, problemType, problemReason,
                currentUserId);
        return Result.success("已标记为问题图书", book);
    }

    /**
     * 将问题图书恢复为正常馆藏
     *
     * @param barcode 条码
     * @return 更新后的馆藏图书信息
     */
    @Operation(summary = "恢复为正常馆藏", description = "将问题图书恢复为正常馆藏")
    @PostMapping("/{barcode}/mark-normal")
    public Result<CollectionBookDTO> markAsNormal(
            @Parameter(description = "条码") @PathVariable String barcode) {
        Long currentUserId = getCurrentUserId();
        CollectionBookDTO book = collectionBookService.markAsNormal(barcode, currentUserId);
        return Result.success("已恢复为正常馆藏", book);
    }

    /**
     * 获取统计信息
     *
     * @return 统计数据
     */
    @Operation(summary = "获取统计信息", description = "获取馆藏图书统计数据（总数、正常馆藏数、问题图书数）")
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = collectionBookService.getStatistics();
        return Result.success(stats);
    }

    /**
     * 获取当前登录用户ID
     * 从 SecurityContext 中获取用户信息
     */
    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 如果有自定义 UserDetails，可以从中获取用户ID
                // 这里暂时返回默认值，实际项目中应该从 UserDetails 中获取
                return 1L;
            }
            return 1L;
        } catch (Exception e) {
            return 1L;
        }
    }
}
