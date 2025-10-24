package com.library.management.module.publisher.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.exception.BusinessException;
import com.library.management.common.utils.ExcelUtil;
import com.library.management.module.publisher.dto.*;
import com.library.management.module.publisher.entity.PublisherWhitelist;
import com.library.management.module.publisher.mapper.PublisherWhitelistMapper;
import com.library.management.module.publisher.service.PublisherWhitelistService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 出版社白名单服务实现类
 *
 * @Service: Spring 服务层注解，标记为业务逻辑组件
 * @RequiredArgsConstructor: Lombok 注解，自动生成包含 final 字段的构造函数（用于依赖注入）
 * @Slf4j: Lombok 注解，自动生成日志对象
 *
 * @author Library Management System
 * @since 2025-10-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherWhitelistServiceImpl implements PublisherWhitelistService {

    private final PublisherWhitelistMapper publisherWhitelistMapper;

    /**
     * 分页查询出版社白名单列表（支持多条件查询）
     *
     * 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件
     * - like: 模糊查询（出版社名称）
     * - eq: 精确查询（年份批次、是否启用）
     */
    @Override
    public Page<PublisherWhitelistDTO> queryPublishers(PublisherWhitelistQueryRequest request) {
        // 创建分页对象
        Page<PublisherWhitelist> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<PublisherWhitelist> wrapper = new LambdaQueryWrapper<>();

        // 出版社名称模糊查询
        if (StringUtils.hasText(request.getPublisherName())) {
            wrapper.like(PublisherWhitelist::getPublisherName, request.getPublisherName());
        }

        // 年份批次精确查询
        if (request.getYears() != null) {
            wrapper.eq(PublisherWhitelist::getYears, request.getYears());
        }

        // 是否启用精确查询
        if (request.getIsActive() != null) {
            wrapper.eq(PublisherWhitelist::getIsActive, request.getIsActive());
        }

        // 按创建时间倒序排序
        wrapper.orderByDesc(PublisherWhitelist::getCreateTime);

        // 执行分页查询
        Page<PublisherWhitelist> publisherPage = publisherWhitelistMapper.selectPage(page, wrapper);

        // 转换为 DTO
        Page<PublisherWhitelistDTO> dtoPage = new Page<>(publisherPage.getCurrent(), publisherPage.getSize(), publisherPage.getTotal());
        dtoPage.setRecords(publisherPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList());

        return dtoPage;
    }

    /**
     * 根据ID查询出版社白名单详情
     */
    @Override
    public PublisherWhitelistDTO getPublisherById(Long publisherId) {
        PublisherWhitelist publisher = publisherWhitelistMapper.selectById(publisherId);
        if (publisher == null) {
            throw new BusinessException("出版社白名单记录不存在");
        }
        return convertToDTO(publisher);
    }

    /**
     * 创建新出版社白名单记录
     *
     * 业务逻辑：
     * 1. 检查出版社名称是否已存在（避免重复添加）
     * 2. 创建出版社白名单对象并保存到数据库
     *
     * @CacheEvict 注解：
     * - 方法执行后清除指定缓存
     * - cacheNames：缓存名称
     * - allEntries = true：清除该缓存下的所有条目
     */
    @Override
    @CacheEvict(cacheNames = "publisherWhitelist", allEntries = true)
    public PublisherWhitelistDTO createPublisher(PublisherWhitelistCreateRequest request, Long createdBy) {
        // 1. 检查出版社名称是否已存在
        PublisherWhitelist existingPublisher = publisherWhitelistMapper.selectByPublisherName(request.getPublisherName());
        if (existingPublisher != null) {
            throw new BusinessException("该出版社已在白名单中");
        }

        // 2. 创建出版社白名单对象
        PublisherWhitelist publisher = PublisherWhitelist.builder()
                .publisherName(request.getPublisherName())
                .years(request.getYears())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(createdBy)
                .build();

        // 3. 保存到数据库
        int rows = publisherWhitelistMapper.insert(publisher);
        if (rows == 0) {
            throw new BusinessException("创建出版社白名单记录失败");
        }

        log.info("创建出版社白名单成功，publisherId: {}, publisherName: {}", publisher.getPublisherId(), publisher.getPublisherName());

        // 4. 返回出版社白名单信息
        return convertToDTO(publisher);
    }

    /**
     * 修改出版社白名单信息
     *
     * 业务逻辑：
     * 1. 检查出版社白名单是否存在
     * 2. 如果修改了出版社名称，检查新名称是否已被其他记录占用
     * 3. 更新出版社白名单信息（只更新非空字段）
     *
     * @CacheEvict：修改后清除缓存
     */
    @Override
    @CacheEvict(cacheNames = "publisherWhitelist", allEntries = true)
    public PublisherWhitelistDTO updatePublisher(PublisherWhitelistUpdateRequest request) {
        // 1. 检查出版社白名单是否存在
        PublisherWhitelist publisher = publisherWhitelistMapper.selectById(request.getPublisherId());
        if (publisher == null) {
            throw new BusinessException("出版社白名单记录不存在");
        }

        // 2. 如果修改了出版社名称，检查新名称是否已被其他记录占用
        if (StringUtils.hasText(request.getPublisherName()) && !request.getPublisherName().equals(publisher.getPublisherName())) {
            PublisherWhitelist existingPublisher = publisherWhitelistMapper.selectByPublisherName(request.getPublisherName());
            if (existingPublisher != null && !existingPublisher.getPublisherId().equals(publisher.getPublisherId())) {
                throw new BusinessException("该出版社名称已被其他记录占用");
            }
            publisher.setPublisherName(request.getPublisherName());
        }

        // 3. 更新其他字段（只更新非空字段）
        if (request.getYears() != null) {
            publisher.setYears(request.getYears());
        }
        if (request.getIsActive() != null) {
            publisher.setIsActive(request.getIsActive());
        }

        // 4. 保存到数据库
        int rows = publisherWhitelistMapper.updateById(publisher);
        if (rows == 0) {
            throw new BusinessException("更新出版社白名单失败");
        }

        log.info("更新出版社白名单成功，publisherId: {}, publisherName: {}", publisher.getPublisherId(), publisher.getPublisherName());

        // 5. 返回更新后的出版社白名单信息
        return convertToDTO(publisher);
    }

    /**
     * 删除出版社白名单记录
     *
     * @CacheEvict：删除后清除缓存
     */
    @Override
    @CacheEvict(cacheNames = "publisherWhitelist", allEntries = true)
    public void deletePublisher(Long publisherId) {
        // 1. 检查出版社白名单是否存在
        PublisherWhitelist publisher = publisherWhitelistMapper.selectById(publisherId);
        if (publisher == null) {
            throw new BusinessException("出版社白名单记录不存在");
        }

        // 2. 执行删除
        int rows = publisherWhitelistMapper.deleteById(publisherId);
        if (rows == 0) {
            throw new BusinessException("删除出版社白名单失败");
        }

        log.info("删除出版社白名单成功，publisherId: {}, publisherName: {}", publisherId, publisher.getPublisherName());
    }

    /**
     * 获取所有启用的出版社白名单（用于缓存）
     *
     * @Cacheable 注解：
     * - 方法执行前先查询缓存，如果缓存中有数据则直接返回
     * - 如果缓存中没有数据，则执行方法并将结果放入缓存
     * - cacheNames：缓存名称
     * - key：缓存键名
     */
    @Override
    @Cacheable(cacheNames = "publisherWhitelist", key = "'all_active'")
    public List<PublisherWhitelistDTO> getAllActivePublishers() {
        // 查询所有启用的出版社白名单
        LambdaQueryWrapper<PublisherWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublisherWhitelist::getIsActive, true)
                .orderByDesc(PublisherWhitelist::getCreateTime);

        List<PublisherWhitelist> publishers = publisherWhitelistMapper.selectList(wrapper);

        return publishers.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 批量导入出版社白名单
     *
     * 业务逻辑：
     * 1. 解析 Excel 文件
     * 2. 校验数据格式
     * 3. 检查重复性（跳过已存在的出版社名称）
     * 4. 批量插入数据库
     * 5. 返回导入结果统计
     */
    @Override
    @CacheEvict(cacheNames = "publisherWhitelist", allEntries = true)
    public Map<String, Object> importPublishers(MultipartFile file, Long createdBy) {
        List<PublisherWhitelistExcelDTO> excelDataList;
        try {
            excelDataList = EasyExcel.read(file.getInputStream())
                    .head(PublisherWhitelistExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            throw new BusinessException("读取 Excel 文件失败：" + e.getMessage());
        }

        if (excelDataList == null || excelDataList.isEmpty()) {
            throw new BusinessException("Excel 文件内容为空");
        }

        // 统计数据
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errorMessages = new ArrayList<>();

        // 逐行处理数据
        for (int i = 0; i < excelDataList.size(); i++) {
            PublisherWhitelistExcelDTO excelDTO = excelDataList.get(i);
            int rowNum = i + 2; // Excel 行号（从第2行开始，第1行是表头）

            try {
                // 1. 校验必填字段
                if (!StringUtils.hasText(excelDTO.getPublisherName())) {
                    errorMessages.add("第" + rowNum + "行：出版社名称不能为空");
                    failCount.incrementAndGet();
                    continue;
                }

                // 2. 检查出版社名称是否已存在
                PublisherWhitelist existingPublisher = publisherWhitelistMapper.selectByPublisherName(excelDTO.getPublisherName().trim());
                if (existingPublisher != null) {
                    errorMessages.add("第" + rowNum + "行：出版社「" + excelDTO.getPublisherName() + "」已存在");
                    failCount.incrementAndGet();
                    continue;
                }

                // 3. 转换 isActive 字段
                Boolean isActive = true;
                if (StringUtils.hasText(excelDTO.getIsActive())) {
                    String activeStr = excelDTO.getIsActive().trim();
                    if ("启用".equals(activeStr) || "是".equals(activeStr) || "true".equalsIgnoreCase(activeStr)) {
                        isActive = true;
                    } else if ("禁用".equals(activeStr) || "否".equals(activeStr) || "false".equalsIgnoreCase(activeStr)) {
                        isActive = false;
                    } else {
                        errorMessages.add("第" + rowNum + "行：是否启用格式错误（应为：启用/禁用）");
                        failCount.incrementAndGet();
                        continue;
                    }
                }

                // 4. 转换 years 字段
                Long years = null;
                if (StringUtils.hasText(excelDTO.getYears())) {
                    try {
                        years = Long.parseLong(excelDTO.getYears().trim());
                    } catch (NumberFormatException e) {
                        errorMessages.add("第" + rowNum + "行：年份批次格式错误（应为数字）");
                        failCount.incrementAndGet();
                        continue;
                    }
                }

                // 5. 创建出版社白名单对象并保存
                PublisherWhitelist publisher = PublisherWhitelist.builder()
                        .publisherName(excelDTO.getPublisherName().trim())
                        .years(years)
                        .isActive(isActive)
                        .createdBy(createdBy)
                        .build();

                publisherWhitelistMapper.insert(publisher);
                successCount.incrementAndGet();

            } catch (Exception e) {
                log.error("导入第{}行数据失败", rowNum, e);
                errorMessages.add("第" + rowNum + "行：导入失败 - " + e.getMessage());
                failCount.incrementAndGet();
            }
        }

        log.info("批量导入出版社白名单完成，成功：{}条，失败：{}条", successCount.get(), failCount.get());

        // 返回导入结果
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount.get());
        result.put("failCount", failCount.get());
        result.put("totalCount", excelDataList.size());
        result.put("errorMessages", errorMessages);

        return result;
    }

    /**
     * 批量导出出版社白名单
     *
     * 导出所有符合查询条件的出版社白名单数据到 Excel 文件
     */
    @Override
    public void exportPublishers(HttpServletResponse response, PublisherWhitelistQueryRequest request) {
        // 1. 查询要导出的数据
        LambdaQueryWrapper<PublisherWhitelist> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getPublisherName())) {
            wrapper.like(PublisherWhitelist::getPublisherName, request.getPublisherName());
        }
        if (request.getYears() != null) {
            wrapper.eq(PublisherWhitelist::getYears, request.getYears());
        }
        if (request.getIsActive() != null) {
            wrapper.eq(PublisherWhitelist::getIsActive, request.getIsActive());
        }

        wrapper.orderByDesc(PublisherWhitelist::getCreateTime);

        List<PublisherWhitelist> publishers = publisherWhitelistMapper.selectList(wrapper);

        // 2. 转换为 Excel DTO
        List<PublisherWhitelistExcelDTO> excelDataList = publishers.stream()
                .map(this::convertToExcelDTO)
                .toList();

        // 3. 设置响应头
        String fileName = "出版社白名单_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 4. 写入 Excel
            EasyExcel.write(response.getOutputStream(), PublisherWhitelistExcelDTO.class)
                    .sheet("出版社白名单")
                    .doWrite(excelDataList);

            log.info("导出出版社白名单成功，共{}条数据", excelDataList.size());

        } catch (IOException e) {
            log.error("导出出版社白名单失败", e);
            throw new BusinessException("导出出版社白名单失败：" + e.getMessage());
        }
    }

    /**
     * 下载出版社白名单导入模板
     *
     * 生成包含示例数据的 Excel 模板文件
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 1. 创建示例数据
        List<PublisherWhitelistExcelDTO> templateData = new ArrayList<>();
        templateData.add(PublisherWhitelistExcelDTO.builder()
                .publisherName("人民教育出版社")
                .years("2024")
                .isActive("启用")
                .build());
        templateData.add(PublisherWhitelistExcelDTO.builder()
                .publisherName("高等教育出版社")
                .years("2024")
                .isActive("启用")
                .build());

        // 2. 设置响应头
        String fileName = "出版社白名单导入模板.xlsx";
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 3. 写入 Excel
            EasyExcel.write(response.getOutputStream(), PublisherWhitelistExcelDTO.class)
                    .sheet("出版社白名单")
                    .doWrite(templateData);

            log.info("下载出版社白名单导入模板成功");

        } catch (IOException e) {
            log.error("下载出版社白名单导入模板失败", e);
            throw new BusinessException("下载模板失败：" + e.getMessage());
        }
    }

    /**
     * 将实体类转换为 DTO
     */
    private PublisherWhitelistDTO convertToDTO(PublisherWhitelist publisher) {
        if (publisher == null) {
            return null;
        }
        PublisherWhitelistDTO dto = new PublisherWhitelistDTO();
        BeanUtils.copyProperties(publisher, dto);
        return dto;
    }

    /**
     * 将实体类转换为 Excel DTO
     */
    private PublisherWhitelistExcelDTO convertToExcelDTO(PublisherWhitelist publisher) {
        if (publisher == null) {
            return null;
        }
        return PublisherWhitelistExcelDTO.builder()
                .publisherName(publisher.getPublisherName())
                .years(publisher.getYears() != null ? publisher.getYears().toString() : "")
                .isActive(publisher.getIsActive() ? "启用" : "禁用")
                .build();
    }
}
