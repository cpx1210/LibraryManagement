package com.library.management.module.sensitiveword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.exception.BusinessException;
import com.library.management.common.utils.ExcelUtil;
import com.library.management.module.sensitiveword.dto.SensitiveWordCreateRequest;
import com.library.management.module.sensitiveword.dto.SensitiveWordDTO;
import com.library.management.module.sensitiveword.dto.SensitiveWordExcelDTO;
import com.library.management.module.sensitiveword.dto.SensitiveWordQueryRequest;
import com.library.management.module.sensitiveword.dto.SensitiveWordUpdateRequest;
import com.library.management.module.sensitiveword.entity.SensitiveWords;
import com.library.management.module.sensitiveword.mapper.SensitiveWordMapper;
import com.library.management.module.sensitiveword.service.SensitiveWordService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 敏感词服务实现类
 *
 * @Service: Spring 服务层注解，标记为业务逻辑组件
 * @RequiredArgsConstructor: Lombok 注解，自动生成包含 final 字段的构造函数（用于依赖注入）
 * @Slf4j: Lombok 注解，自动生成日志对象
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    /**
     * 分页查询敏感词列表
     *
     * 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件
     * - like: 模糊查询
     * - eq: 精确查询
     */
    @Override
    public Page<SensitiveWordDTO> queryWords(SensitiveWordQueryRequest request) {
        // 创建分页对象
        Page<SensitiveWords> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();

        // 敏感词内容模糊查询
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(SensitiveWords::getKeyword, request.getKeyword());
        }

        // 类别精确查询
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(SensitiveWords::getCategory, request.getCategory());
        }

        // 创建人模糊查询
        if (StringUtils.hasText(request.getCreatedBy())) {
            wrapper.like(SensitiveWords::getCreatedBy, request.getCreatedBy());
        }

        // 按创建时间倒序排序
        wrapper.orderByDesc(SensitiveWords::getCreateTime);

        // 执行分页查询
        Page<SensitiveWords> wordPage = sensitiveWordMapper.selectPage(page, wrapper);

        // 转换为 DTO
        Page<SensitiveWordDTO> dtoPage = new Page<>(wordPage.getCurrent(), wordPage.getSize(), wordPage.getTotal());
        dtoPage.setRecords(wordPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList());

        return dtoPage;
    }

    /**
     * 根据ID查询敏感词详情
     */
    @Override
    public SensitiveWordDTO getWordById(Long wordId) {
        SensitiveWords word = sensitiveWordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException("敏感词不存在");
        }
        return convertToDTO(word);
    }

    /**
     * 创建新敏感词
     *
     * 业务逻辑：
     * 1. 检查敏感词内容是否已存在
     * 2. 创建敏感词对象并保存到数据库
     *
     * @CacheEvict 注解：
     * - 方法执行后清除指定缓存
     * - cacheNames：缓存名称
     * - allEntries = true：清除该缓存下的所有条目
     */
    @Override
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public SensitiveWordDTO createWord(SensitiveWordCreateRequest request, String createdBy) {
        // 1. 检查敏感词是否已存在
        SensitiveWords existingWord = sensitiveWordMapper.selectByKeyword(request.getKeyword());
        if (existingWord != null) {
            throw new BusinessException("敏感词已存在");
        }

        // 2. 创建敏感词对象
        SensitiveWords word = SensitiveWords.builder()
                .keyword(request.getKeyword())
                .category(request.getCategory())
                .createdBy(createdBy)
                .build();

        // 3. 保存到数据库
        int rows = sensitiveWordMapper.insert(word);
        if (rows == 0) {
            throw new BusinessException("创建敏感词失败");
        }

        // 4. 返回敏感词信息
        return convertToDTO(word);
    }

    /**
     * 修改敏感词信息
     *
     * 业务逻辑：
     * 1. 检查敏感词是否存在
     * 2. 如果修改敏感词内容，检查新内容是否已被其他敏感词占用
     * 3. 更新敏感词信息（只更新非空字段）
     *
     * @CacheEvict：修改后清除缓存
     */
    @Override
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public SensitiveWordDTO updateWord(SensitiveWordUpdateRequest request, String updatedBy) {
        // 1. 检查敏感词是否存在
        SensitiveWords word = sensitiveWordMapper.selectById(request.getWordId());
        if (word == null) {
            throw new BusinessException("敏感词不存在");
        }

        // 2. 如果修改了敏感词内容，检查新内容是否已被其他敏感词占用
        if (StringUtils.hasText(request.getKeyword()) && !request.getKeyword().equals(word.getKeyword())) {
            SensitiveWords existingWord = sensitiveWordMapper.selectByKeyword(request.getKeyword());
            if (existingWord != null) {
                throw new BusinessException("敏感词已被占用");
            }
            word.setKeyword(request.getKeyword());
        }

        // 3. 更新其他字段（只更新非空字段）
        if (StringUtils.hasText(request.getCategory())) {
            word.setCategory(request.getCategory());
        }

        // 4. 设置更新人和更新时间
        word.setUpdatedBy(updatedBy);
        word.setUpdateTime(LocalDateTime.now());

        // 5. 保存到数据库
        int rows = sensitiveWordMapper.updateById(word);
        if (rows == 0) {
            throw new BusinessException("修改敏感词失败");
        }

        // 6. 返回修改后的敏感词信息
        return convertToDTO(word);
    }

    /**
     * 删除敏感词
     *
     * 业务逻辑：
     * 1. 检查敏感词是否存在
     * 2. 执行物理删除（从数据库中删除记录）
     *
     * @CacheEvict：删除后清除缓存
     */
    @Override
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public void deleteWord(Long wordId) {
        // 1. 检查敏感词是否存在
        SensitiveWords word = sensitiveWordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException("敏感词不存在");
        }

        // 2. 执行删除
        int rows = sensitiveWordMapper.deleteById(wordId);
        if (rows == 0) {
            throw new BusinessException("删除敏感词失败");
        }
    }

    /**
     * 批量导入敏感词
     *
     * 业务逻辑：
     * 1. 读取 Excel 文件
     * 2. 校验数据（必填字段检查、重复性检查）
     * 3. 批量插入数据库
     * 4. 返回导入结果统计
     *
     * @CacheEvict：导入后清除缓存
     */
    @Override
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public Map<String, Object> importWords(MultipartFile file, String createdBy) {
        // 1. 读取 Excel 文件
        List<SensitiveWordExcelDTO> excelDataList = ExcelUtil.read(file, SensitiveWordExcelDTO.class);

        if (excelDataList == null || excelDataList.isEmpty()) {
            throw new BusinessException("Excel 文件为空或格式不正确");
        }

        // 2. 数据校验和转换
        List<SensitiveWords> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        AtomicInteger rowIndex = new AtomicInteger(1); // 从第2行开始（第1行是表头）

        excelDataList.forEach(excelData -> {
            rowIndex.incrementAndGet();
            try {
                // 校验必填字段
                if (!StringUtils.hasText(excelData.getKeyword())) {
                    errorList.add("第 " + rowIndex.get() + " 行：敏感词内容不能为空");
                    return;
                }
                if (!StringUtils.hasText(excelData.getCategory())) {
                    errorList.add("第 " + rowIndex.get() + " 行：敏感词类别不能为空");
                    return;
                }

                // 检查是否已存在
                SensitiveWords existingWord = sensitiveWordMapper.selectByKeyword(excelData.getKeyword());
                if (existingWord != null) {
                    errorList.add("第 " + rowIndex.get() + " 行：敏感词「" + excelData.getKeyword() + "」已存在");
                    return;
                }

                // 转换为实体对象
                SensitiveWords word = SensitiveWords.builder()
                        .keyword(excelData.getKeyword())
                        .category(excelData.getCategory())
                        .createdBy(createdBy)
                        .build();

                successList.add(word);
            } catch (Exception e) {
                log.error("处理第 {} 行数据失败", rowIndex.get(), e);
                errorList.add("第 " + rowIndex.get() + " 行：" + e.getMessage());
            }
        });

        // 3. 批量插入数据库
        int successCount = 0;
        if (!successList.isEmpty()) {
            for (SensitiveWords word : successList) {
                try {
                    sensitiveWordMapper.insert(word);
                    successCount++;
                } catch (Exception e) {
                    log.error("插入敏感词失败: {}", word.getKeyword(), e);
                    errorList.add("敏感词「" + word.getKeyword() + "」保存失败：" + e.getMessage());
                }
            }
        }

        // 4. 返回导入结果统计
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", excelDataList.size());
        result.put("successCount", successCount);
        result.put("errorCount", errorList.size());
        result.put("errorList", errorList);

        log.info("敏感词导入完成：总数 {}, 成功 {}, 失败 {}",
                excelDataList.size(), successCount, errorList.size());

        return result;
    }

    /**
     * 批量导出敏感词
     *
     * 业务逻辑：
     * 1. 根据查询条件获取数据（如果没有条件则导出全部）
     * 2. 转换为 Excel DTO
     * 3. 使用 ExcelUtil 写入响应流
     */
    @Override
    public void exportWords(HttpServletResponse response, SensitiveWordQueryRequest request) {
        // 1. 查询数据
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();

        // 如果提供了查询条件，则按条件过滤
        if (request != null) {
            if (StringUtils.hasText(request.getKeyword())) {
                wrapper.like(SensitiveWords::getKeyword, request.getKeyword());
            }
            if (StringUtils.hasText(request.getCategory())) {
                wrapper.eq(SensitiveWords::getCategory, request.getCategory());
            }
            if (StringUtils.hasText(request.getCreatedBy())) {
                wrapper.like(SensitiveWords::getCreatedBy, request.getCreatedBy());
            }
        }

        wrapper.orderByDesc(SensitiveWords::getCreateTime);
        List<SensitiveWords> wordList = sensitiveWordMapper.selectList(wrapper);

        // 2. 转换为 Excel DTO
        List<SensitiveWordExcelDTO> excelDataList = wordList.stream()
                .map(word -> SensitiveWordExcelDTO.builder()
                        .keyword(word.getKeyword())
                        .category(word.getCategory())
                        .build())
                .toList();

        // 3. 导出 Excel
        ExcelUtil.write(response, excelDataList, SensitiveWordExcelDTO.class, "敏感词列表");

        log.info("敏感词导出完成，共 {} 条数据", excelDataList.size());
    }

    /**
     * 下载敏感词导入模板
     *
     * 生成一个包含示例数据的模板文件
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 创建示例数据
        List<SensitiveWordExcelDTO> templateData = new ArrayList<>();
        templateData.add(SensitiveWordExcelDTO.builder()
                .keyword("示例敏感词1")
                .category("政治")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .keyword("示例敏感词2")
                .category("色情")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .keyword("示例敏感词3")
                .category("暴力")
                .build());

        // 导出模板
        ExcelUtil.write(response, templateData, SensitiveWordExcelDTO.class, "敏感词导入模板");

        log.info("敏感词导入模板下载完成");
    }

    /**
     * 获取所有敏感词（用于缓存）
     *
     * @Cacheable 注解：
     * - 方法执行前先查询缓存
     * - 如果缓存中有数据，直接返回，不执行方法
     * - 如果缓存中没有数据，执行方法并将结果存入缓存
     * - cacheNames：缓存名称
     * - key：缓存的 key，这里使用固定值 "all"
     */
    @Override
    @Cacheable(cacheNames = "sensitiveWords", key = "'all'")
    public List<SensitiveWordDTO> getAllWords() {
        log.info("从数据库加载所有敏感词（未命中缓存）");

        // 查询所有敏感词
        List<SensitiveWords> wordList = sensitiveWordMapper.selectList(null);

        // 转换为 DTO
        List<SensitiveWordDTO> dtoList = wordList.stream()
                .map(this::convertToDTO)
                .toList();

        log.info("加载敏感词完成，共 {} 条", dtoList.size());
        return dtoList;
    }

    /**
     * 实体转 DTO
     */
    private SensitiveWordDTO convertToDTO(SensitiveWords word) {
        SensitiveWordDTO dto = new SensitiveWordDTO();
        BeanUtils.copyProperties(word, dto);
        return dto;
    }
}
