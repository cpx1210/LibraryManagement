package com.library.management.module.collectionbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.collectionbook.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 馆藏图书服务接口
 * 定义馆藏图书管理的业务方法
 *
 * @author Library Management System
 * @since 2025-12-05
 */
public interface CollectionBookService {

    /**
     * 分页查询馆藏图书列表（支持多条件查询）
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Page<CollectionBookDTO> queryBooks(CollectionBookQueryRequest request);

    /**
     * 根据条码查询馆藏图书详情
     *
     * @param barcode 条码
     * @return 馆藏图书信息
     */
    CollectionBookDTO getBookByBarcode(String barcode);

    /**
     * 创建新馆藏图书
     *
     * @param request   创建请求
     * @param createdBy 创建人用户ID
     * @return 创建的馆藏图书信息
     */
    CollectionBookDTO createBook(CollectionBookCreateRequest request, Long createdBy);

    /**
     * 修改馆藏图书信息
     *
     * @param request   修改请求
     * @param updatedBy 修改人用户ID
     * @return 修改后的馆藏图书信息
     */
    CollectionBookDTO updateBook(CollectionBookUpdateRequest request, Long updatedBy);

    /**
     * 删除馆藏图书
     *
     * @param barcode 条码
     */
    void deleteBook(String barcode);

    /**
     * 批量删除馆藏图书
     *
     * @param barcodes 条码列表
     * @return 删除数量
     */
    int deleteBatchBooks(List<String> barcodes);

    /**
     * 获取所有正常馆藏图书
     *
     * @return 正常馆藏图书列表
     */
    List<CollectionBookDTO> getAllNormalBooks();

    /**
     * 获取所有问题图书
     *
     * @return 问题图书列表
     */
    List<CollectionBookDTO> getAllProblemBooks();

    /**
     * 批量导入馆藏图书
     *
     * @param file      上传的 Excel 文件
     * @param createdBy 创建人用户ID
     * @return 导入结果统计
     */
    Map<String, Object> importBooks(MultipartFile file, Long createdBy);

    /**
     * 批量导出馆藏图书
     *
     * @param response HTTP 响应对象
     * @param request  查询条件（可选，导出符合条件的数据）
     */
    void exportBooks(HttpServletResponse response, CollectionBookQueryRequest request);

    /**
     * 下载馆藏图书导入模板
     *
     * @param response HTTP 响应对象
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 将馆藏图书标记为问题图书
     *
     * @param barcode       条码
     * @param problemType   问题类型
     * @param problemReason 问题原因
     * @param updatedBy     操作人用户ID
     * @return 更新后的馆藏图书信息
     */
    CollectionBookDTO markAsProblem(String barcode, String problemType, String problemReason, Long updatedBy);

    /**
     * 将问题图书恢复为正常馆藏
     *
     * @param barcode   条码
     * @param updatedBy 操作人用户ID
     * @return 更新后的馆藏图书信息
     */
    CollectionBookDTO markAsNormal(String barcode, Long updatedBy);

    /**
     * 统计信息
     *
     * @return 统计数据（总数、正常馆藏数、问题图书数）
     */
    Map<String, Long> getStatistics();
}
