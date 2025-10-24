package com.library.management.module.publisher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.module.publisher.dto.PublisherWhitelistCreateRequest;
import com.library.management.module.publisher.dto.PublisherWhitelistDTO;
import com.library.management.module.publisher.dto.PublisherWhitelistQueryRequest;
import com.library.management.module.publisher.dto.PublisherWhitelistUpdateRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 出版社白名单服务接口
 * 定义出版社白名单管理的业务方法
 *
 * @author Library Management System
 * @since 2025-10-24
 */
public interface PublisherWhitelistService {

    /**
     * 分页查询出版社白名单列表（支持多条件查询）
     *
     * @param request 查询条件（出版社名称、年份批次、是否启用）
     * @return 分页结果
     */
    Page<PublisherWhitelistDTO> queryPublishers(PublisherWhitelistQueryRequest request);

    /**
     * 根据ID查询出版社白名单详情
     *
     * @param publisherId 出版社ID
     * @return 出版社白名单信息
     */
    PublisherWhitelistDTO getPublisherById(Long publisherId);

    /**
     * 创建新出版社白名单记录
     *
     * @param request 出版社白名单创建请求
     * @param createdBy 创建人用户ID
     * @return 创建的出版社白名单信息
     */
    PublisherWhitelistDTO createPublisher(PublisherWhitelistCreateRequest request, Long createdBy);

    /**
     * 修改出版社白名单信息
     *
     * @param request 出版社白名单修改请求
     * @return 修改后的出版社白名单信息
     */
    PublisherWhitelistDTO updatePublisher(PublisherWhitelistUpdateRequest request);

    /**
     * 删除出版社白名单记录
     *
     * @param publisherId 出版社ID
     */
    void deletePublisher(Long publisherId);

    /**
     * 获取所有启用的出版社白名单（用于缓存）
     *
     * @return 所有启用的出版社白名单列表
     */
    List<PublisherWhitelistDTO> getAllActivePublishers();

    /**
     * 批量导入出版社白名单
     *
     * @param file 上传的 Excel 文件
     * @param createdBy 创建人用户ID
     * @return 导入结果统计
     */
    Map<String, Object> importPublishers(MultipartFile file, Long createdBy);

    /**
     * 批量导出出版社白名单
     *
     * @param response HTTP 响应对象
     * @param request 查询条件（可选，导出符合条件的数据）
     */
    void exportPublishers(HttpServletResponse response, PublisherWhitelistQueryRequest request);

    /**
     * 下载出版社白名单导入模板
     *
     * @param response HTTP 响应对象
     */
    void downloadTemplate(HttpServletResponse response);
}
