package com.library.management.module.problembook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.result.Result;
import com.library.management.module.problembook.dto.ProblemBookCreateRequest;
import com.library.management.module.problembook.dto.ProblemBookDTO;
import com.library.management.module.problembook.dto.ProblemBookQueryRequest;
import com.library.management.module.problembook.dto.ProblemBookUpdateRequest;
import com.library.management.module.problembook.service.ProblemBookService;
import io.swagger.v3.oas.annotations.Operation;
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
 * 问题书目管理控制器
 *
 * @Tag: Swagger 文档标签
 * @RestController: 组合注解，等同于 @Controller + @ResponseBody
 * @RequestMapping: 定义基础路径为 /problem-books
 * @RequiredArgsConstructor: Lombok 注解，自动注入 final 字段
 *
 *                           RESTful API 设计规范：
 *                           - GET /problem-books：查询列表
 *                           - GET /problem-books/{bookId}：查询详情
 *                           - POST /problem-books：创建新记录
 *                           - PUT /problem-books：更新记录
 *                           - DELETE /problem-books/{bookId}：删除记录
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Tag(name = "问题书目管理", description = "问题书目增删改查接口")
@RestController
@RequestMapping("/problem-books")
@RequiredArgsConstructor
public class ProblemBookController {

    private final ProblemBookService problemBookService;

    /**
     * 分页查询问题书目列表（支持多条件查询）
     *
     * @param request 查询条件（自动绑定查询参数）
     * @return 分页结果
     *
     *         支持的查询条件：
     *         - bookName：书名（模糊查询）
     *         - author：作者（模糊查询）
     *         - isbn：ISBN 编号（精确查询）
     *         - publisher：出版社（模糊查询）
     *         - publishYear：出版年份（精确查询）
     *         - problemType：问题类型（模糊查询）
     *         - pageNum：页码（默认第1页）
     *         - pageSize：每页大小（默认10条）
     */
    @Operation(summary = "查询问题书目列表", description = "支持分页和多条件查询（书名、作者、ISBN、出版社、出版年份、问题类型）")
    @GetMapping
    public Result<Page<ProblemBookDTO>> queryBooks(ProblemBookQueryRequest request) {
        Page<ProblemBookDTO> page = problemBookService.queryBooks(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询问题书目详情
     *
     * @param bookId 书目ID（路径参数）
     * @return 问题书目信息
     */
    @Operation(summary = "查询问题书目详情", description = "根据书目ID获取详细信息")
    @GetMapping("/{bookId}")
    public Result<ProblemBookDTO> getBookById(@PathVariable Long bookId) {
        ProblemBookDTO book = problemBookService.getBookById(bookId);
        return Result.success(book);
    }

    /**
     * 创建新问题书目
     *
     * @param request 问题书目创建请求（JSON 请求体）
     * @return 创建的问题书目信息
     *
     * @Valid: 启用 Bean Validation 参数校验
     * @RequestBody: 接收 JSON 格式的请求体
     *
     *               请求体示例：
     *               {
     *               "bookName": "问题图书名称",
     *               "author": "作者",
     *               "isbn": "9787111111111",
     *               "publisher": "出版社",
     *               "publishYear": "2023",
     *               "problemType": "政治问题",
     *               "source": "教育部通报"
     *               }
     */
    @Operation(summary = "创建新问题书目", description = "新增问题书目")
    @PostMapping
    public Result<ProblemBookDTO> createBook(@Valid @RequestBody ProblemBookCreateRequest request) {
        // TODO: 获取当前登录用户ID（暂时使用固定值 1L）
        // 实际应从 SecurityContext 或 JWT Token 中获取当前用户ID
        Long currentUserId = 1L;
        ProblemBookDTO book = problemBookService.createBook(request, currentUserId);
        return Result.success("问题书目创建成功", book);
    }

    /**
     * 修改问题书目信息
     *
     * @param request 问题书目修改请求（JSON 请求体）
     * @return 修改后的问题书目信息
     *
     *         请求体示例：
     *         {
     *         "bookId": 1,
     *         "bookName": "修改后的书名",
     *         "author": "修改后的作者",
     *         "problemType": "内容不当"
     *         }
     *
     *         说明：
     *         - bookId 必填，用于指定要修改的问题书目
     *         - 其他字段可选，只更新提供的字段
     */
    @Operation(summary = "修改问题书目信息", description = "更新问题书目信息（部分更新）")
    @PostMapping("/update")
    public Result<ProblemBookDTO> updateBook(@Valid @RequestBody ProblemBookUpdateRequest request) {
        // TODO: 获取当前登录用户ID（暂时使用固定值 1L）
        Long currentUserId = 1L;
        ProblemBookDTO book = problemBookService.updateBook(request, currentUserId);
        return Result.success("问题书目修改成功", book);
    }

    /**
     * 删除问题书目
     *
     * @param bookId 书目ID（路径参数）
     * @return 删除结果
     */
    @Operation(summary = "删除问题书目", description = "物理删除问题书目（不可恢复）")
    @PostMapping("/delete/{bookId}")
    public Result<Void> deleteBook(@PathVariable Long bookId) {
        problemBookService.deleteBook(bookId);
        return Result.success("问题书目删除成功", null);
    }

    /**
     * 批量导入问题书目
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果统计
     */
    @Operation(summary = "批量导入问题书目", description = "通过 Excel 文件批量导入问题书目")
    @PostMapping("/import")
    public Result<Map<String, Object>> importBooks(@RequestParam("file") MultipartFile file) {
        // TODO: 获取当前登录用户ID（暂时使用固定值 1L）
        Long currentUserId = 1L;
        Map<String, Object> result = problemBookService.importBooks(file, currentUserId);
        return Result.success("导入完成", result);
    }

    /**
     * 批量导出问题书目
     *
     * @param response HTTP 响应对象
     * @param request  查询条件（可选）
     */
    @Operation(summary = "批量导出问题书目", description = "导出问题书目为 Excel 文件")
    @GetMapping("/export")
    public void exportBooks(HttpServletResponse response, ProblemBookQueryRequest request) {
        problemBookService.exportBooks(response, request);
    }

    /**
     * 下载问题书目导入模板
     *
     * @param response HTTP 响应对象
     */
    @Operation(summary = "下载导入模板", description = "下载问题书目导入模板文件")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        problemBookService.downloadTemplate(response);
    }

    /**
     * 获取所有问题书目（用于书单检测等场景）
     *
     * @return 所有问题书目列表
     */
    @Operation(summary = "获取所有问题书目", description = "获取完整的问题书目列表（不分页）")
    @GetMapping("/all")
    public Result<List<ProblemBookDTO>> getAllBooks() {
        List<ProblemBookDTO> books = problemBookService.getAllBooks();
        return Result.success(books);
    }
}
