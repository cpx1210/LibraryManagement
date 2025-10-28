package com.library.management.module.sensitiveword.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.sensitiveword.dto.SensitiveWordCreateRequest;
import com.library.management.module.sensitiveword.dto.SensitiveWordDTO;
import com.library.management.module.sensitiveword.dto.SensitiveWordExcelDTO;
import com.library.management.module.sensitiveword.dto.SensitiveWordQueryRequest;
import com.library.management.module.sensitiveword.dto.SensitiveWordUpdateRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 敏感词服务接口
 * 定义敏感词管理的业务方法
 *
 * @author Library Management System
 * @since 2025-10-21
 */
public interface SensitiveWordService {

    /**
     * 分页查询敏感词列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Page<SensitiveWordDTO> queryWords(SensitiveWordQueryRequest request);

    /**
     * 根据ID查询敏感词详情
     *
     * @param wordId 敏感词ID
     * @return 敏感词信息
     */
    SensitiveWordDTO getWordById(Long wordId);

    /**
     * 创建新敏感词
     *
     * @param request 敏感词创建请求
     * @param createdBy 创建人用户ID
     * @return 创建的敏感词信息
     */
    SensitiveWordDTO createWord(SensitiveWordCreateRequest request, Long createdBy);

    /**
     * 修改敏感词信息
     *
     * @param request 敏感词修改请求
     * @param updatedBy 修改人用户ID
     * @return 修改后的敏感词信息
     */
    SensitiveWordDTO updateWord(SensitiveWordUpdateRequest request, Long updatedBy);

    /**
     * 删除敏感词
     *
     * @param wordId 敏感词ID
     */
    void deleteWord(Long wordId);

    /**
     * 批量导入敏感词
     *
     * @param file 上传的 Excel 文件
     * @param createdBy 创建人用户ID
     * @return 导入结果统计
     */
    Map<String, Object> importWords(MultipartFile file, Long createdBy);

    /**
     * 批量导出敏感词
     *
     * @param response HTTP 响应对象
     * @param request 查询条件（可选，导出符合条件的数据）
     */
    void exportWords(HttpServletResponse response, SensitiveWordQueryRequest request);

    /**
     * 下载敏感词导入模板
     *
     * @param response HTTP 响应对象
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 获取所有敏感词（用于缓存）
     *
     * @return 所有敏感词列表
     */
    List<SensitiveWordDTO> getAllWords();

    /**
     * 获取所有敏感词分类
     *
     * @return 所有分类列表（包含 categoryId 和 categoryName）
     */
    List<Map<String, Object>> getAllCategories();
}
