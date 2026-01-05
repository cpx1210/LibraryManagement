package com.library.management.module.sensitiveword.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.result.Result;
import com.library.management.module.sensitiveword.dto.SensitiveWordCreateRequest;
import com.library.management.module.sensitiveword.dto.SensitiveWordDTO;
import com.library.management.module.sensitiveword.dto.SensitiveWordQueryRequest;
import com.library.management.module.sensitiveword.dto.SensitiveWordUpdateRequest;
import com.library.management.module.sensitiveword.service.SensitiveWordService;
import com.library.management.module.user.service.UserService;
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
 * 敏感词管理控制器
 *
 * @Tag: Swagger 文档标签
 * @RestController: 组合注解，等同于 @Controller + @ResponseBody
 * @RequestMapping: 定义基础路径为 /sensitive-words
 * @RequiredArgsConstructor: Lombok 注解，自动注入 final 字段
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Tag(name = "敏感词管理", description = "敏感词增删改查接口")
@RestController
@RequestMapping("/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;
    private final UserService userService;

    /**
     * 分页查询敏感词列表
     *
     * @param request 查询条件（自动绑定查询参数）
     * @return 分页结果
     */
    @Operation(summary = "查询敏感词列表", description = "支持分页和多条件查询")
    @GetMapping
    public Result<Page<SensitiveWordDTO>> queryWords(SensitiveWordQueryRequest request) {
        Page<SensitiveWordDTO> page = sensitiveWordService.queryWords(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询敏感词详情
     *
     * @param wordId 敏感词ID（路径参数）
     * @return 敏感词信息
     */
    @Operation(summary = "查询敏感词详情", description = "根据敏感词ID获取详细信息")
    @GetMapping("/{wordId}")
    public Result<SensitiveWordDTO> getWordById(@PathVariable Long wordId) {
        SensitiveWordDTO word = sensitiveWordService.getWordById(wordId);
        return Result.success(word);
    }

    /**
     * 创建新敏感词
     *
     * @param request 敏感词创建请求（JSON 请求体）
     * @return 创建的敏感词信息
     *
     * @Valid: 启用 Bean Validation 参数校验
     * @RequestBody: 接收 JSON 格式的请求体
     */
    @Operation(summary = "创建新敏感词", description = "新增敏感词")
    @PostMapping
    public Result<SensitiveWordDTO> createWord(@Valid @RequestBody SensitiveWordCreateRequest request) {
        // 获取当前登录用户名
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // 根据用户名获取用户ID
        Long currentUserId = userService.getUserIdByUsername(currentUsername);
        SensitiveWordDTO word = sensitiveWordService.createWord(request, currentUserId);
        return Result.success("敏感词创建成功", word);
    }

    /**
     * 修改敏感词信息
     *
     * @param request 敏感词修改请求（JSON 请求体）
     * @return 修改后的敏感词信息
     */
    @Operation(summary = "修改敏感词信息", description = "更新敏感词信息")
    @PostMapping("/update")
    public Result<SensitiveWordDTO> updateWord(@Valid @RequestBody SensitiveWordUpdateRequest request) {
        // 获取当前登录用户名
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // 根据用户名获取用户ID
        Long currentUserId = userService.getUserIdByUsername(currentUsername);
        SensitiveWordDTO word = sensitiveWordService.updateWord(request, currentUserId);
        return Result.success("敏感词修改成功", word);
    }

    /**
     * 删除敏感词
     *
     * @param wordId 敏感词ID（路径参数）
     * @return 删除结果
     */
    @Operation(summary = "删除敏感词", description = "物理删除敏感词（不可恢复）")
    @PostMapping("/delete/{wordId}")
    public Result<Void> deleteWord(@PathVariable Long wordId) {
        sensitiveWordService.deleteWord(wordId);
        return Result.success("敏感词删除成功", null);
    }

    /**
     * 批量导入敏感词
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果统计
     */
    @Operation(summary = "批量导入敏感词", description = "通过 Excel 文件批量导入敏感词")
    @PostMapping("/import")
    public Result<Map<String, Object>> importWords(@RequestParam("file") MultipartFile file) {
        // 获取当前登录用户名
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        // 根据用户名获取用户ID
        Long currentUserId = userService.getUserIdByUsername(currentUsername);
        Map<String, Object> result = sensitiveWordService.importWords(file, currentUserId);
        return Result.success("导入完成", result);
    }

    /**
     * 批量导出敏感词
     *
     * @param response HTTP 响应对象
     * @param request  查询条件（可选）
     */
    @Operation(summary = "批量导出敏感词", description = "导出敏感词为 Excel 文件")
    @GetMapping("/export")
    public void exportWords(HttpServletResponse response, SensitiveWordQueryRequest request) {
        sensitiveWordService.exportWords(response, request);
    }

    /**
     * 下载敏感词导入模板
     *
     * @param response HTTP 响应对象
     */
    @Operation(summary = "下载导入模板", description = "下载敏感词导入模板文件")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        sensitiveWordService.downloadTemplate(response);
    }

    /**
     * 获取所有敏感词（带缓存）
     *
     * @return 所有敏感词列表
     *
     *         使用场景：
     *         - 前端展示敏感词全量数据
     *         - 内部敏感词检测功能
     */
    @Operation(summary = "获取所有敏感词", description = "获取所有敏感词（带缓存优化）")
    @GetMapping("/all")
    public Result<List<SensitiveWordDTO>> getAllWords() {
        List<SensitiveWordDTO> words = sensitiveWordService.getAllWords();
        return Result.success(words);
    }

    /**
     * 获取所有敏感词分类
     *
     * @return 所有分类列表
     *
     *         使用场景：
     *         - 前端下拉框选择分类
     *         - 前端筛选条件
     */
    @Operation(summary = "获取所有敏感词分类", description = "获取所有敏感词分类列表")
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getAllCategories() {
        List<Map<String, Object>> categories = sensitiveWordService.getAllCategories();
        return Result.success(categories);
    }
}
