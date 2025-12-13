package com.library.management.module.sensitiveword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.json.JSONUtil;
import com.library.management.common.annotation.Log;
import com.library.management.common.exception.BusinessException;
import com.library.management.common.utils.ExcelUtil;
import com.library.management.module.log.aspect.LogAspect;
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
    private final com.library.management.module.sensitiveword.mapper.SensitiveCategoryMapper sensitiveCategoryMapper;

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

        // 检测类型精确查询
        if (StringUtils.hasText(request.getDetectionType())) {
            wrapper.eq(SensitiveWords::getDetectionType, request.getDetectionType());
        }

        // 分类ID精确查询
        if (request.getCategoryId() != null) {
            wrapper.eq(SensitiveWords::getCategoryId, request.getCategoryId());
        }

        // 匹配类型精确查询
        if (request.getMatchType() != null) {
            wrapper.eq(SensitiveWords::getMatchType, request.getMatchType());
        }

        // 风险等级精确查询
        if (request.getRiskLevel() != null) {
            wrapper.eq(SensitiveWords::getRiskLevel, request.getRiskLevel());
        }

        // 是否启用精确查询
        if (request.getIsActive() != null) {
            wrapper.eq(SensitiveWords::getIsActive, request.getIsActive());
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
     * 1. 验证分类ID是否存在
     * 2. 检查敏感词内容是否已存在
     * 3. 创建敏感词对象并保存到数据库
     *
     * @CacheEvict 注解：
     *             - 方法执行后清除指定缓存
     *             - cacheNames：缓存名称
     *             - allEntries = true：清除该缓存下的所有条目
     */
    @Override
    @Log(module = "sensitive_word", operationType = "create")
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public SensitiveWordDTO createWord(SensitiveWordCreateRequest request, Long createdBy) {
        // 1. 验证分类ID是否存在（如果提供了的话）
        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            var category = sensitiveCategoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException("敏感词分类不存在");
            }
        } else {
            // 默认使用第一个分类
            categoryId = 1L;
        }

        // 2. 检查敏感词是否已存在
        SensitiveWords existingWord = sensitiveWordMapper.selectByKeyword(request.getKeyword());
        if (existingWord != null) {
            throw new BusinessException("敏感词已存在");
        }

        // 3. 创建敏感词对象
        SensitiveWords word = SensitiveWords.builder()
                .keyword(request.getKeyword())
                .categoryId(categoryId)
                .detectionType(request.getDetectionType()) // 检测类型
                .alertMessage(request.getAlertMessage()) // 警报信息
                .matchType(request.getMatchType())
                .riskLevel(request.getRiskLevel())
                .isActive(request.getIsActive())
                .createdBy(createdBy)
                .build();

        // 4. 保存到数据库
        int rows = sensitiveWordMapper.insert(word);
        if (rows == 0) {
            throw new BusinessException("创建敏感词失败");
        }

        // 5. 返回敏感词信息
        return convertToDTO(word);
    }

    /**
     * 修改敏感词信息
     *
     * 业务逻辑：
     * 1. 检查敏感词是否存在
     * 2. 如果修改分类ID，验证分类是否存在
     * 3. 如果修改敏感词内容，检查新内容是否已被其他敏感词占用
     * 4. 更新敏感词信息（只更新非空字段）
     *
     * @CacheEvict：修改后清除缓存
     */
    @Override
    @Log(module = "sensitive_word", operationType = "update")
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public SensitiveWordDTO updateWord(SensitiveWordUpdateRequest request, Long updatedBy) {
        // 1. 检查敏感词是否存在
        SensitiveWords word = sensitiveWordMapper.selectById(request.getWordId());
        if (word == null) {
            throw new BusinessException("敏感词不存在");
        }

        // 记录原始数据（用于日志）
        LogAspect.setOldValue(JSONUtil.toJsonStr(word));

        // 2. 如果修改了分类ID，验证分类是否存在
        if (request.getCategoryId() != null) {
            var category = sensitiveCategoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException("敏感词分类不存在");
            }
            word.setCategoryId(request.getCategoryId());
        }

        // 3. 如果修改了敏感词内容，检查新内容是否已被其他敏感词占用
        if (StringUtils.hasText(request.getKeyword()) && !request.getKeyword().equals(word.getKeyword())) {
            SensitiveWords existingWord = sensitiveWordMapper.selectByKeyword(request.getKeyword());
            if (existingWord != null) {
                throw new BusinessException("敏感词已被占用");
            }
            word.setKeyword(request.getKeyword());
        }

        // 4. 更新其他字段（只更新非空字段）
        if (StringUtils.hasText(request.getDetectionType())) {
            word.setDetectionType(request.getDetectionType());
        }
        if (request.getAlertMessage() != null) {
            word.setAlertMessage(request.getAlertMessage());
        }
        if (request.getMatchType() != null) {
            word.setMatchType(request.getMatchType());
        }
        if (request.getRiskLevel() != null) {
            word.setRiskLevel(request.getRiskLevel());
        }
        if (request.getIsActive() != null) {
            word.setIsActive(request.getIsActive());
        }

        // 5. 设置更新人和更新时间
        word.setUpdatedBy(updatedBy);
        word.setUpdateTime(LocalDateTime.now());

        // 6. 保存到数据库
        int rows = sensitiveWordMapper.updateById(word);
        if (rows == 0) {
            throw new BusinessException("修改敏感词失败");
        }

        // 7. 返回修改后的敏感词信息
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
    @Log(module = "sensitive_word", operationType = "delete")
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public void deleteWord(Long wordId) {
        // 1. 检查敏感词是否存在
        SensitiveWords word = sensitiveWordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException("敏感词不存在");
        }

        // 记录原始数据（用于日志）
        LogAspect.setOldValue(JSONUtil.toJsonStr(word));

        // 2. 执行删除
        int rows = sensitiveWordMapper.deleteById(wordId);
        if (rows == 0) {
            throw new BusinessException("删除敏感词失败");
        }
    }

    /**
     * 批量导入敏感词（更新版 - 适配新模板）
     *
     * 业务逻辑：
     * 1. 读取 Excel 文件（新格式：类型、关键词、警报信息）
     * 2. 校验数据（必填字段检查、类型校验、重复性检查）
     * 3. 批量插入数据库
     * 4. 返回导入结果统计
     *
     * 新模板列结构：
     * - 类型（关键词/书名/作者）
     * - 关键词
     * - 警报信息
     *
     * @CacheEvict：导入后清除缓存
     */
    @Override
    @Log(module = "sensitive_word", operationType = "import")
    @CacheEvict(cacheNames = "sensitiveWords", allEntries = true)
    public Map<String, Object> importWords(MultipartFile file, Long createdBy) {
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
                // 校验必填字段：类型和关键词
                if (!StringUtils.hasText(excelData.getDetectionType())) {
                    errorList.add("第 " + rowIndex.get() + " 行：类型不能为空（必须是：关键词、书名、作者之一）");
                    return;
                }
                if (!StringUtils.hasText(excelData.getKeyword())) {
                    errorList.add("第 " + rowIndex.get() + " 行：关键词不能为空");
                    return;
                }

                // 校验类型是否合法
                String detectionType = excelData.getDetectionType().trim();
                if (!detectionType.equals("关键词") && !detectionType.equals("书名") && !detectionType.equals("作者")) {
                    errorList.add("第 " + rowIndex.get() + " 行：类型「" + detectionType + "」无效，必须是：关键词、书名、作者之一");
                    return;
                }

                // 检查敏感词是否已存在
                SensitiveWords existingWord = sensitiveWordMapper.selectByKeyword(excelData.getKeyword().trim());
                if (existingWord != null) {
                    errorList.add("第 " + rowIndex.get() + " 行：关键词「" + excelData.getKeyword() + "」已存在");
                    return;
                }

                // 解析风险等级（从文本转换为数字）
                Integer riskLevel = 2; // 默认中风险
                if (StringUtils.hasText(excelData.getRiskLevel())) {
                    String riskLevelText = excelData.getRiskLevel().trim();
                    switch (riskLevelText) {
                        case "低风险":
                            riskLevel = 1;
                            break;
                        case "中风险":
                            riskLevel = 2;
                            break;
                        case "高风险":
                            riskLevel = 3;
                            break;
                        default:
                            errorList.add("第 " + rowIndex.get() + " 行：风险等级「" + riskLevelText
                                    + "」无效，必须是：低风险、中风险、高风险之一（将使用默认值：中风险）");
                            // 不return，继续处理，使用默认值
                            break;
                    }
                }

                // 转换为实体对象
                SensitiveWords word = SensitiveWords.builder()
                        .keyword(excelData.getKeyword().trim())
                        .detectionType(detectionType)
                        .riskLevel(riskLevel) // 使用解析后的风险等级
                        .alertMessage(StringUtils.hasText(excelData.getAlertMessage())
                                ? excelData.getAlertMessage().trim()
                                : null)
                        .categoryId(1L) // 默认分类ID（可根据实际情况调整）
                        .matchType(1) // 默认模糊匹配
                        .isActive(true) // 默认启用
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
                    errorList.add("关键词「" + word.getKeyword() + "」保存失败：" + e.getMessage());
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
     * 批量导出敏感词（更新版 - 适配新模板）
     *
     * 业务逻辑：
     * 1. 根据查询条件获取数据（如果没有条件则导出全部）
     * 2. 转换为 Excel DTO（新格式：类型、关键词、警报信息）
     * 3. 使用 ExcelUtil 写入响应流
     *
     * 导出列结构：
     * - 类型（关键词/书名/作者）
     * - 关键词
     * - 警报信息
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
            if (request.getCategoryId() != null) {
                wrapper.eq(SensitiveWords::getCategoryId, request.getCategoryId());
            }
            if (request.getMatchType() != null) {
                wrapper.eq(SensitiveWords::getMatchType, request.getMatchType());
            }
            if (request.getRiskLevel() != null) {
                wrapper.eq(SensitiveWords::getRiskLevel, request.getRiskLevel());
            }
            if (request.getIsActive() != null) {
                wrapper.eq(SensitiveWords::getIsActive, request.getIsActive());
            }
        }

        wrapper.orderByDesc(SensitiveWords::getCreateTime);
        List<SensitiveWords> wordList = sensitiveWordMapper.selectList(wrapper);

        // 2. 转换为 Excel DTO（新格式）
        List<SensitiveWordExcelDTO> excelDataList = wordList.stream()
                .map(word -> {
                    // 将风险等级数字转换为文本
                    String riskLevelText = "中风险"; // 默认值
                    if (word.getRiskLevel() != null) {
                        switch (word.getRiskLevel()) {
                            case 1:
                                riskLevelText = "低风险";
                                break;
                            case 2:
                                riskLevelText = "中风险";
                                break;
                            case 3:
                                riskLevelText = "高风险";
                                break;
                        }
                    }

                    return SensitiveWordExcelDTO.builder()
                            .detectionType(word.getDetectionType() != null ? word.getDetectionType() : "关键词")
                            .keyword(word.getKeyword())
                            .riskLevel(riskLevelText)
                            .alertMessage(word.getAlertMessage())
                            .build();
                })
                .toList();

        // 3. 导出 Excel
        ExcelUtil.write(response, excelDataList, SensitiveWordExcelDTO.class, "敏感词列表");

        log.info("敏感词导出完成，共 {} 条数据", excelDataList.size());
    }

    /**
     * 下载敏感词导入模板（更新版 - 适配新模板）
     *
     * 生成一个包含示例数据的模板文件
     *
     * 模板列结构：
     * - 类型（关键词/书名/作者）
     * - 关键词
     * - 风险等级（低风险/中风险/高风险）
     * - 警报信息
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 创建示例数据（参考实际敏感词数据）
        List<SensitiveWordExcelDTO> templateData = new ArrayList<>();

        // 关键词类型示例（展示不同风险等级）
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("关键词")
                .keyword("澳门博彩")
                .riskLevel("高风险")
                .alertMessage("疑似赌博相关")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("关键词")
                .keyword("法轮功")
                .riskLevel("高风险")
                .alertMessage("疑似敏感政治内容")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("关键词")
                .keyword("警察&腐败")
                .riskLevel("中风险")
                .alertMessage("疑似敏感政治内容")
                .build());

        // 书名类型示例
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("书名")
                .keyword("儿子与情人")
                .riskLevel("中风险")
                .alertMessage("疑似不良价值观内容")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("书名")
                .keyword("一个女人的史诗")
                .riskLevel("高风险")
                .alertMessage("疑似敏感政治内容")
                .build());

        // 作者类型示例
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("作者")
                .keyword("白先勇")
                .riskLevel("低风险")
                .alertMessage("疑似敏感政治内容")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("作者")
                .keyword("蔡英文")
                .riskLevel("高风险")
                .alertMessage("疑似敏感政治内容")
                .build());
        templateData.add(SensitiveWordExcelDTO.builder()
                .detectionType("作者")
                .keyword("柴静")
                .riskLevel("中风险")
                .alertMessage("疑似敏感政治内容")
                .build());

        // 导出模板
        ExcelUtil.write(response, templateData, SensitiveWordExcelDTO.class, "敏感词导入模板");

        log.info("敏感词导入模板下载完成");
    }

    /**
     * 获取所有敏感词（用于缓存）
     *
     * @Cacheable 注解：
     *            - 方法执行前先查询缓存
     *            - 如果缓存中有数据，直接返回，不执行方法
     *            - 如果缓存中没有数据，执行方法并将结果存入缓存
     *            - cacheNames：缓存名称
     *            - key：缓存的 key，这里使用固定值 "all"
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
     * 获取所有敏感词分类
     *
     * @return 所有分类列表（包含
     *         categoryId 和 categoryName）
     */
    @Override
    public List<Map<String, Object>> getAllCategories() {
        log.info("查询所有敏感词分类");

        // 查询所有分类
        var categories = sensitiveCategoryMapper.selectList(null);

        // 转换为 Map 列表，方便前端使用
        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId",
                    category.getCategoryId());

            map.put("categoryName",
                    category.getCategoryName());
            map.put("description",
                    category.getDescription());
            return map;
        })
                .toList();

        log.info("查询分类完成，共 {} 条",
                result.size());
        return result;
    }

    /**
     * 实体转 DTO（包含关联查询分类名称）
     */
    private SensitiveWordDTO convertToDTO(SensitiveWords word) {
        SensitiveWordDTO dto = new SensitiveWordDTO();
        BeanUtils.copyProperties(word, dto);

        // 关联查询分类名称
        if (word.getCategoryId() != null) {
            var category = sensitiveCategoryMapper.selectById(word.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
        }

        return dto;
    }
}
