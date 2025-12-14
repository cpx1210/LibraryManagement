package com.library.management.module.problembook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.json.JSONUtil;
import com.library.management.common.annotation.Log;
import com.library.management.common.exception.BusinessException;
import com.library.management.common.utils.ExcelUtil;
import com.library.management.module.log.aspect.LogAspect;
import com.library.management.module.problembook.dto.ProblemBookCreateRequest;
import com.library.management.module.problembook.dto.ProblemBookDTO;
import com.library.management.module.problembook.dto.ProblemBookExcelDTO;
import com.library.management.module.problembook.dto.ProblemBookQueryRequest;
import com.library.management.module.problembook.dto.ProblemBookUpdateRequest;
import com.library.management.module.problembook.entity.ProblemBook;
import com.library.management.module.problembook.mapper.ProblemBookMapper;
import com.library.management.module.problembook.service.ProblemBookService;
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
 * 问题书目服务实现类
 *
 * @Service: Spring 服务层注解，标记为业务逻辑组件
 * @RequiredArgsConstructor: Lombok 注解，自动生成包含 final 字段的构造函数（用于依赖注入）
 * @Slf4j: Lombok 注解，自动生成日志对象
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemBookServiceImpl implements ProblemBookService {

    private final ProblemBookMapper problemBookMapper;

    /**
     * 分页查询问题书目列表（支持多条件查询）
     *
     * 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件
     * - like: 模糊查询（书名、作者、出版社、问题类型）
     * - eq: 精确查询（ISBN、出版年份）
     */
    @Override
    public Page<ProblemBookDTO> queryBooks(ProblemBookQueryRequest request) {
        // 创建分页对象
        Page<ProblemBook> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<ProblemBook> wrapper = new LambdaQueryWrapper<>();

        // 书名模糊查询
        if (StringUtils.hasText(request.getBookName())) {
            wrapper.like(ProblemBook::getBookName, request.getBookName());
        }

        // 作者模糊查询
        if (StringUtils.hasText(request.getAuthor())) {
            wrapper.like(ProblemBook::getAuthor, request.getAuthor());
        }

        // ISBN 精确查询
        if (StringUtils.hasText(request.getIsbn())) {
            wrapper.eq(ProblemBook::getIsbn, request.getIsbn());
        }

        // 出版社模糊查询
        if (StringUtils.hasText(request.getPublisher())) {
            wrapper.like(ProblemBook::getPublisher, request.getPublisher());
        }

        // 出版年份精确查询
        if (StringUtils.hasText(request.getPublishYear())) {
            wrapper.eq(ProblemBook::getPublishYear, request.getPublishYear());
        }

        // 问题类型模糊查询
        if (StringUtils.hasText(request.getProblemType())) {
            wrapper.like(ProblemBook::getProblemType, request.getProblemType());
        }

        // 按创建时间倒序排序
        wrapper.orderByDesc(ProblemBook::getCreateTime);

        // 执行分页查询
        Page<ProblemBook> bookPage = problemBookMapper.selectPage(page, wrapper);

        // 转换为 DTO
        Page<ProblemBookDTO> dtoPage = new Page<>(bookPage.getCurrent(), bookPage.getSize(), bookPage.getTotal());
        dtoPage.setRecords(bookPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList());

        return dtoPage;
    }

    /**
     * 根据ID查询问题书目详情
     */
    @Override
    public ProblemBookDTO getBookById(Long bookId) {
        ProblemBook book = problemBookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("问题书目不存在");
        }
        return convertToDTO(book);
    }

    /**
     * 创建新问题书目
     *
     * 业务逻辑：
     * 1. 检查书名或 ISBN 是否已存在（避免重复添加）
     * 2. 创建问题书目对象并保存到数据库
     *
     * @CacheEvict 注解：
     *             - 方法执行后清除指定缓存
     *             - cacheNames：缓存名称
     *             - allEntries = true：清除该缓存下的所有条目
     */
    @Override
    @Log(module = "problem_book", operationType = "create")
    @CacheEvict(cacheNames = "problemBooks", allEntries = true)
    public ProblemBookDTO createBook(ProblemBookCreateRequest request, Long createdBy) {
        // 1. 检查 ISBN 是否已存在（如果提供了 ISBN）
        if (StringUtils.hasText(request.getIsbn())) {
            ProblemBook existingBook = problemBookMapper.selectByIsbn(request.getIsbn());
            if (existingBook != null) {
                throw new BusinessException("该 ISBN 的问题书目已存在");
            }
        }

        // 2. 检查书名是否已存在
        ProblemBook existingBook = problemBookMapper.selectByBookName(request.getBookName());
        if (existingBook != null) {
            throw new BusinessException("该书名的问题书目已存在");
        }

        // 3. 创建问题书目对象
        ProblemBook book = ProblemBook.builder()
                .bookName(request.getBookName())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publisher(request.getPublisher())
                .publishYear(request.getPublishYear())
                .problemType(request.getProblemType())
                .source(request.getSource())
                .createdBy(createdBy)
                .build();

        // 4. 保存到数据库
        int rows = problemBookMapper.insert(book);
        if (rows == 0) {
            throw new BusinessException("创建问题书目失败");
        }

        log.info("创建问题书目成功，bookId: {}, bookName: {}", book.getBookId(), book.getBookName());

        // 5. 返回问题书目信息
        return convertToDTO(book);
    }

    /**
     * 修改问题书目信息
     *
     * 业务逻辑：
     * 1. 检查问题书目是否存在
     * 2. 如果修改了书名或 ISBN，检查新值是否已被其他书目占用
     * 3. 更新问题书目信息（只更新非空字段）
     *
     * @CacheEvict：修改后清除缓存
     */
    @Override
    @Log(module = "problem_book", operationType = "update")
    @CacheEvict(cacheNames = "problemBooks", allEntries = true)
    public ProblemBookDTO updateBook(ProblemBookUpdateRequest request, Long updatedBy) {
        // 1. 检查问题书目是否存在
        ProblemBook book = problemBookMapper.selectById(request.getBookId());
        if (book == null) {
            throw new BusinessException("问题书目不存在");
        }

        // 记录原始数据（用于日志）
        LogAspect.setOldValue(JSONUtil.toJsonStr(book));

        // 2. 如果修改了 ISBN，检查新 ISBN 是否已被其他书目占用
        if (StringUtils.hasText(request.getIsbn()) && !request.getIsbn().equals(book.getIsbn())) {
            ProblemBook existingBook = problemBookMapper.selectByIsbn(request.getIsbn());
            if (existingBook != null && !existingBook.getBookId().equals(book.getBookId())) {
                throw new BusinessException("该 ISBN 已被其他问题书目占用");
            }
            book.setIsbn(request.getIsbn());
        }

        // 3. 如果修改了书名，检查新书名是否已被其他书目占用
        if (StringUtils.hasText(request.getBookName()) && !request.getBookName().equals(book.getBookName())) {
            ProblemBook existingBook = problemBookMapper.selectByBookName(request.getBookName());
            if (existingBook != null && !existingBook.getBookId().equals(book.getBookId())) {
                throw new BusinessException("该书名已被其他问题书目占用");
            }
            book.setBookName(request.getBookName());
        }

        // 4. 更新其他字段（只更新非空字段）
        if (StringUtils.hasText(request.getAuthor())) {
            book.setAuthor(request.getAuthor());
        }
        if (StringUtils.hasText(request.getPublisher())) {
            book.setPublisher(request.getPublisher());
        }
        if (StringUtils.hasText(request.getPublishYear())) {
            book.setPublishYear(request.getPublishYear());
        }
        if (StringUtils.hasText(request.getProblemType())) {
            book.setProblemType(request.getProblemType());
        }
        if (StringUtils.hasText(request.getSource())) {
            book.setSource(request.getSource());
        }

        // 5. 设置更新人和更新时间
        book.setUpdatedBy(updatedBy);
        book.setUpdateTime(LocalDateTime.now());

        // 6. 保存到数据库
        int rows = problemBookMapper.updateById(book);
        if (rows == 0) {
            throw new BusinessException("更新问题书目失败");
        }

        log.info("更新问题书目成功，bookId: {}, bookName: {}", book.getBookId(), book.getBookName());

        // 7. 返回更新后的问题书目信息
        return convertToDTO(book);
    }

    /**
     * 删除问题书目
     *
     * @CacheEvict：删除后清除缓存
     */
    @Override
    @Log(module = "problem_book", operationType = "delete")
    @CacheEvict(cacheNames = "problemBooks", allEntries = true)
    public void deleteBook(Long bookId) {
        // 1. 检查问题书目是否存在
        ProblemBook book = problemBookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("问题书目不存在");
        }

        // 记录原始数据（用于日志）
        LogAspect.setOldValue(JSONUtil.toJsonStr(book));

        // 2. 删除问题书目
        int rows = problemBookMapper.deleteById(bookId);
        if (rows == 0) {
            throw new BusinessException("删除问题书目失败");
        }

        log.info("删除问题书目成功，bookId: {}, bookName: {}", bookId, book.getBookName());
    }

    /**
     * 获取所有问题书目（用于缓存）
     *
     * @Cacheable 注解：
     *            - 首次调用时查询数据库并缓存结果
     *            - 后续调用直接从缓存获取
     *            - cacheNames：缓存名称
     */
    @Override
    @Cacheable(cacheNames = "problemBooks")
    public List<ProblemBookDTO> getAllBooks() {
        log.info("从数据库加载所有问题书目");
        List<ProblemBook> books = problemBookMapper.selectList(null);
        return books.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 批量导入问题书目
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
    @Log(module = "problem_book", operationType = "import")
    @CacheEvict(cacheNames = "problemBooks", allEntries = true)
    public Map<String, Object> importBooks(MultipartFile file, Long createdBy) {
        // 1. 读取 Excel 文件
        List<ProblemBookExcelDTO> excelDataList = ExcelUtil.read(file, ProblemBookExcelDTO.class);

        if (excelDataList == null || excelDataList.isEmpty()) {
            throw new BusinessException("Excel 文件为空或格式不正确");
        }

        // 2. 数据校验和转换
        List<ProblemBook> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        AtomicInteger rowIndex = new AtomicInteger(1); // 从第2行开始（第1行是表头）

        excelDataList.forEach(excelData -> {
            rowIndex.incrementAndGet();
            try {
                // 校验必填字段：书名
                if (!StringUtils.hasText(excelData.getBookName())) {
                    errorList.add("第 " + rowIndex.get() + " 行：书名不能为空");
                    return;
                }

                // 检查书名是否已存在
                ProblemBook existingByName = problemBookMapper.selectByBookName(excelData.getBookName());
                if (existingByName != null) {
                    errorList.add("第 " + rowIndex.get() + " 行：书名「" + excelData.getBookName() + "」已存在");
                    return;
                }

                // 检查 ISBN 是否已存在（如果提供了 ISBN）
                if (StringUtils.hasText(excelData.getIsbn())) {
                    ProblemBook existingByIsbn = problemBookMapper.selectByIsbn(excelData.getIsbn());
                    if (existingByIsbn != null) {
                        errorList.add("第 " + rowIndex.get() + " 行：ISBN「" + excelData.getIsbn() + "」已存在");
                        return;
                    }
                }

                // 转换为实体对象
                ProblemBook book = ProblemBook.builder()
                        .bookName(excelData.getBookName())
                        .author(excelData.getAuthor())
                        .isbn(excelData.getIsbn())
                        .publisher(excelData.getPublisher())
                        .publishYear(excelData.getPublishYear())
                        .problemType(excelData.getProblemType())
                        .source(excelData.getSource())
                        .createdBy(createdBy)
                        .build();

                successList.add(book);
            } catch (Exception e) {
                log.error("处理第 {} 行数据失败", rowIndex.get(), e);
                errorList.add("第 " + rowIndex.get() + " 行：" + e.getMessage());
            }
        });

        // 3. 批量插入数据库
        int successCount = 0;
        if (!successList.isEmpty()) {
            for (ProblemBook book : successList) {
                try {
                    problemBookMapper.insert(book);
                    successCount++;
                } catch (Exception e) {
                    log.error("插入问题书目失败: {}", book.getBookName(), e);
                    errorList.add("问题书目「" + book.getBookName() + "」保存失败：" + e.getMessage());
                }
            }
        }

        // 4. 返回导入结果统计
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", excelDataList.size());
        result.put("successCount", successCount);
        result.put("errorCount", errorList.size());
        result.put("errorList", errorList);

        log.info("问题书目导入完成：总数 {}, 成功 {}, 失败 {}",
                excelDataList.size(), successCount, errorList.size());

        return result;
    }

    /**
     * 批量导出问题书目
     *
     * 业务逻辑：
     * 1. 根据查询条件获取数据（如果没有条件则导出全部）
     * 2. 转换为 Excel DTO
     * 3. 使用 ExcelUtil 写入响应流
     */
    @Override
    @Log(module = "problem_book", operationType = "export")
    public void exportBooks(HttpServletResponse response, ProblemBookQueryRequest request) {
        // 1. 查询数据
        LambdaQueryWrapper<ProblemBook> wrapper = new LambdaQueryWrapper<>();

        // 如果提供了查询条件，则按条件过滤
        if (request != null) {
            if (StringUtils.hasText(request.getBookName())) {
                wrapper.like(ProblemBook::getBookName, request.getBookName());
            }
            if (StringUtils.hasText(request.getAuthor())) {
                wrapper.like(ProblemBook::getAuthor, request.getAuthor());
            }
            if (StringUtils.hasText(request.getIsbn())) {
                wrapper.eq(ProblemBook::getIsbn, request.getIsbn());
            }
            if (StringUtils.hasText(request.getPublisher())) {
                wrapper.like(ProblemBook::getPublisher, request.getPublisher());
            }
            if (StringUtils.hasText(request.getPublishYear())) {
                wrapper.eq(ProblemBook::getPublishYear, request.getPublishYear());
            }
            if (StringUtils.hasText(request.getProblemType())) {
                wrapper.like(ProblemBook::getProblemType, request.getProblemType());
            }
        }

        wrapper.orderByDesc(ProblemBook::getCreateTime);
        List<ProblemBook> bookList = problemBookMapper.selectList(wrapper);

        // 2. 转换为 Excel DTO
        List<ProblemBookExcelDTO> excelDataList = bookList.stream()
                .map(book -> ProblemBookExcelDTO.builder()
                        .bookName(book.getBookName())
                        .author(book.getAuthor())
                        .isbn(book.getIsbn())
                        .publisher(book.getPublisher())
                        .publishYear(book.getPublishYear())
                        .problemType(book.getProblemType())
                        .source(book.getSource())
                        .build())
                .toList();

        // 3. 导出 Excel
        ExcelUtil.write(response, excelDataList, ProblemBookExcelDTO.class, "问题书目列表");

        log.info("问题书目导出完成，共 {} 条数据", excelDataList.size());
    }

    /**
     * 下载问题书目导入模板
     *
     * 生成一个包含示例数据的模板文件
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 创建示例数据
        List<ProblemBookExcelDTO> templateData = new ArrayList<>();
        templateData.add(ProblemBookExcelDTO.builder()
                .bookName("示例问题书目1")
                .author("作者1")
                .isbn("9787111111111")
                .publisher("示例出版社1")
                .publishYear("2023")
                .problemType("政治问题")
                .source("教育部通报")
                .build());
        templateData.add(ProblemBookExcelDTO.builder()
                .bookName("示例问题书目2")
                .author("作者2")
                .isbn("9787222222222")
                .publisher("示例出版社2")
                .publishYear("2024")
                .problemType("内容不当")
                .source("出版社通知")
                .build());
        templateData.add(ProblemBookExcelDTO.builder()
                .bookName("示例问题书目3")
                .author("作者3")
                .isbn("")
                .publisher("示例出版社3")
                .publishYear("2023")
                .problemType("盗版")
                .source("读者举报")
                .build());

        // 导出模板
        ExcelUtil.write(response, templateData, ProblemBookExcelDTO.class, "问题书目导入模板");

        log.info("问题书目导入模板下载完成");
    }

    /**
     * 实体类转换为 DTO
     */
    private ProblemBookDTO convertToDTO(ProblemBook book) {
        ProblemBookDTO dto = new ProblemBookDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }
}
