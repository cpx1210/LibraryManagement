package com.library.management.module.problembook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.problembook.dto.ProblemBookCreateRequest;
import com.library.management.module.problembook.dto.ProblemBookDTO;
import com.library.management.module.problembook.dto.ProblemBookQueryRequest;
import com.library.management.module.problembook.dto.ProblemBookUpdateRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 问题书目服务接口
 * 定义问题书目管理的业务方法
 *
 * @author Library Management System
 * @since 2025-10-22
 */
public interface ProblemBookService {

    /**
     * 分页查询问题书目列表（支持多条件查询）
     *
     * @param request 查询条件（书名、作者、ISBN、出版社、出版年份、问题类型）
     * @return 分页结果
     */
    Page<ProblemBookDTO> queryBooks(ProblemBookQueryRequest request);

    /**
     * 根据ID查询问题书目详情
     *
     * @param bookId 书目ID
     * @return 问题书目信息
     */
    ProblemBookDTO getBookById(Long bookId);

    /**
     * 创建新问题书目
     *
     * @param request 问题书目创建请求
     * @param createdBy 创建人用户ID
     * @return 创建的问题书目信息
     */
    ProblemBookDTO createBook(ProblemBookCreateRequest request, Long createdBy);

    /**
     * 修改问题书目信息
     *
     * @param request 问题书目修改请求
     * @param updatedBy 修改人用户ID
     * @return 修改后的问题书目信息
     */
    ProblemBookDTO updateBook(ProblemBookUpdateRequest request, Long updatedBy);

    /**
     * 删除问题书目
     *
     * @param bookId 书目ID
     */
    void deleteBook(Long bookId);

    /**
     * 获取所有问题书目（用于缓存）
     *
     * @return 所有问题书目列表
     */
    List<ProblemBookDTO> getAllBooks();

    /**
     * 批量导入问题书目
     *
     * @param file 上传的 Excel 文件
     * @param createdBy 创建人用户ID
     * @return 导入结果统计
     */
    Map<String, Object> importBooks(MultipartFile file, Long createdBy);

    /**
     * 批量导出问题书目
     *
     * @param response HTTP 响应对象
     * @param request 查询条件（可选，导出符合条件的数据）
     */
    void exportBooks(HttpServletResponse response, ProblemBookQueryRequest request);

    /**
     * 下载问题书目导入模板
     *
     * @param response HTTP 响应对象
     */
    void downloadTemplate(HttpServletResponse response);
}
