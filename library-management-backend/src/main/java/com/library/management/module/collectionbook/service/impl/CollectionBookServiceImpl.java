package com.library.management.module.collectionbook.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.annotation.Log;
import com.library.management.common.exception.BusinessException;
import com.library.management.module.collectionbook.dto.*;
import com.library.management.module.collectionbook.entity.CollectionBook;
import com.library.management.module.collectionbook.mapper.CollectionBookMapper;
import com.library.management.module.collectionbook.service.CollectionBookService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 馆藏图书服务实现类
 *
 * @author Library Management System
 * @since 2025-12-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionBookServiceImpl implements CollectionBookService {

    private final CollectionBookMapper collectionBookMapper;

    @Override
    public Page<CollectionBookDTO> queryBooks(CollectionBookQueryRequest request) {
        if (useProblemAndBranchFastPath(request)) {
            return queryBooksByProblemAndBranch(request);
        }
        // 1. 创建分页对象
        Page<CollectionBook> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<CollectionBook> queryWrapper = new LambdaQueryWrapper<>();
        boolean includeProblemReason = Integer.valueOf(1).equals(request.getIsProblem());
        selectListColumns(queryWrapper, includeProblemReason);

        // 条码精确查询
        if (StringUtils.hasText(request.getBarcode())) {
            queryWrapper.eq(CollectionBook::getBarcode, request.getBarcode());
        }
        // 题名模糊查询
        if (StringUtils.hasText(request.getBookName())) {
            queryWrapper.like(CollectionBook::getBookName, request.getBookName());
        }
        // 著者模糊查询
        if (StringUtils.hasText(request.getAuthor())) {
            queryWrapper.like(CollectionBook::getAuthor, request.getAuthor());
        }
        // ISBN 精确查询
        if (StringUtils.hasText(request.getIsbn())) {
            queryWrapper.eq(CollectionBook::getIsbn, request.getIsbn());
        }
        // 出版社模糊查询
        if (StringUtils.hasText(request.getPublisher())) {
            queryWrapper.like(CollectionBook::getPublisher, request.getPublisher());
        }
        // 出版年精确查询
        if (StringUtils.hasText(request.getPublishYear())) {
            queryWrapper.eq(CollectionBook::getPublishYear, request.getPublishYear());
        }
        // 分馆精确查询
        if (StringUtils.hasText(request.getBranchLibrary())) {
            queryWrapper.eq(CollectionBook::getBranchLibrary, request.getBranchLibrary());
        }
        // 索书号模糊查询
        if (StringUtils.hasText(request.getCallNumber())) {
            queryWrapper.like(CollectionBook::getCallNumber, request.getCallNumber());
        }
        // 批次精确查询
        if (StringUtils.hasText(request.getBatch())) {
            queryWrapper.eq(CollectionBook::getBatch, request.getBatch());
        }
        // 是否入库精确查询
        if (request.getIsStored() != null) {
            queryWrapper.eq(CollectionBook::getIsStored, request.getIsStored());
        }
        // 馆藏院舍模糊查询
        if (StringUtils.hasText(request.getLibraryLocation())) {
            queryWrapper.like(CollectionBook::getLibraryLocation, request.getLibraryLocation());
        }
        // 重复标记精确查询
        if (request.getDuplicateFlag() != null) {
            queryWrapper.eq(CollectionBook::getDuplicateFlag, request.getDuplicateFlag());
        }
        // 是否问题图书精确查询（关键条件，用于区分馆藏图书和问题图书列表）
        if (request.getIsProblem() != null) {
            queryWrapper.eq(CollectionBook::getIsProblem, request.getIsProblem());
        }
        // 问题类型模糊查询
        if (StringUtils.hasText(request.getProblemType())) {
            queryWrapper.like(CollectionBook::getProblemType, request.getProblemType());
        }

        // 3. 按创建时间倒序排列
        queryWrapper.orderByDesc(CollectionBook::getCreateTime);

        // 4. 执行分页查询
        Page<CollectionBook> resultPage = collectionBookMapper.selectPage(page, queryWrapper);

        // 5. 转换为 DTO
        Page<CollectionBookDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        List<CollectionBookDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public CollectionBookDTO getBookByBarcode(String barcode) {
        CollectionBook book = collectionBookMapper.selectById(barcode);
        if (book == null) {
            throw new BusinessException("馆藏图书不存在，条码：" + barcode);
        }
        return convertToDTO(book);
    }

    @Override
    @Log(module = "collection_book", operationType = "create")
    @Transactional
    public CollectionBookDTO createBook(CollectionBookCreateRequest request, Long createdBy) {
        // 1. 检查条码是否已存在
        if (collectionBookMapper.countByBarcode(request.getBarcode()) > 0) {
            throw new BusinessException("条码已存在：" + request.getBarcode());
        }

        // 2. 创建实体对象
        CollectionBook book = new CollectionBook();
        BeanUtils.copyProperties(request, book);
        book.setCreatedBy(createdBy);
        book.setCreateTime(LocalDateTime.now());

        // 3. 插入数据库
        collectionBookMapper.insert(book);

        log.info("创建馆藏图书成功，条码：{}", book.getBarcode());
        return convertToDTO(book);
    }

    @Override
    @Log(module = "collection_book", operationType = "update")
    @Transactional
    public CollectionBookDTO updateBook(CollectionBookUpdateRequest request, Long updatedBy) {
        // 1. 检查图书是否存在
        CollectionBook existingBook = collectionBookMapper.selectById(request.getBarcode());
        if (existingBook == null) {
            throw new BusinessException("馆藏图书不存在，条码：" + request.getBarcode());
        }

        // 2. 更新字段（只更新非空字段）
        if (StringUtils.hasText(request.getBookName())) {
            existingBook.setBookName(request.getBookName());
        }
        if (StringUtils.hasText(request.getAuthor())) {
            existingBook.setAuthor(request.getAuthor());
        }
        if (StringUtils.hasText(request.getIsbn())) {
            existingBook.setIsbn(request.getIsbn());
        }
        if (StringUtils.hasText(request.getPublisher())) {
            existingBook.setPublisher(request.getPublisher());
        }
        if (StringUtils.hasText(request.getPublishYear())) {
            existingBook.setPublishYear(request.getPublishYear());
        }
        if (StringUtils.hasText(request.getBranchLibrary())) {
            existingBook.setBranchLibrary(request.getBranchLibrary());
        }
        if (StringUtils.hasText(request.getCallNumber())) {
            existingBook.setCallNumber(request.getCallNumber());
        }
        if (request.getPrice() != null) {
            existingBook.setPrice(request.getPrice());
        }
        if (StringUtils.hasText(request.getBatch())) {
            existingBook.setBatch(request.getBatch());
        }
        if (request.getIsStored() != null) {
            existingBook.setIsStored(request.getIsStored());
        }
        if (StringUtils.hasText(request.getLibraryLocation())) {
            existingBook.setLibraryLocation(request.getLibraryLocation());
        }
        if (StringUtils.hasText(request.getShelfLocation())) {
            existingBook.setShelfLocation(request.getShelfLocation());
        }
        if (request.getDuplicateFlag() != null) {
            existingBook.setDuplicateFlag(request.getDuplicateFlag());
        }
        if (request.getIsProblem() != null) {
            existingBook.setIsProblem(request.getIsProblem());
        }
        if (StringUtils.hasText(request.getProblemType())) {
            existingBook.setProblemType(request.getProblemType());
        }
        if (request.getProblemReason() != null) {
            existingBook.setProblemReason(request.getProblemReason());
        }

        existingBook.setUpdatedBy(updatedBy);
        existingBook.setUpdateTime(LocalDateTime.now());

        // 3. 更新数据库
        collectionBookMapper.updateById(existingBook);

        log.info("更新馆藏图书成功，条码：{}", existingBook.getBarcode());
        return convertToDTO(existingBook);
    }

    @Override
    @Log(module = "collection_book", operationType = "delete")
    @Transactional
    public void deleteBook(String barcode) {
        CollectionBook book = collectionBookMapper.selectById(barcode);
        if (book == null) {
            throw new BusinessException("馆藏图书不存在，条码：" + barcode);
        }
        collectionBookMapper.deleteById(barcode);
        log.info("删除馆藏图书成功，条码：{}", barcode);
    }

    @Override
    @Log(module = "collection_book", operationType = "delete")
    @Transactional
    public int deleteBatchBooks(List<String> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) {
            return 0;
        }
        int deleted = collectionBookMapper.deleteBatchIds(barcodes);
        log.info("批量删除馆藏图书成功，数量：{}", deleted);
        return deleted;
    }

    @Override
    public List<CollectionBookDTO> getAllNormalBooks() {
        LambdaQueryWrapper<CollectionBook> queryWrapper = new LambdaQueryWrapper<>();
        selectListColumns(queryWrapper, false);
        queryWrapper.eq(CollectionBook::getIsProblem, 0);
        queryWrapper.orderByDesc(CollectionBook::getCreateTime);
        List<CollectionBook> books = collectionBookMapper.selectList(queryWrapper);
        return books.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CollectionBookDTO> getAllProblemBooks() {
        LambdaQueryWrapper<CollectionBook> queryWrapper = new LambdaQueryWrapper<>();
        selectListColumns(queryWrapper, true);
        queryWrapper.eq(CollectionBook::getIsProblem, 1);
        queryWrapper.orderByDesc(CollectionBook::getCreateTime);
        List<CollectionBook> books = collectionBookMapper.selectList(queryWrapper);
        return books.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Log(module = "collection_book", operationType = "import")
    @Transactional
    public Map<String, Object> importBooks(MultipartFile file, Long createdBy) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<CollectionBook> successList = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), CollectionBookExcelDTO.class,
                    new ReadListener<CollectionBookExcelDTO>() {
                        private int rowIndex = 1; // 从第2行开始（第1行是表头）

                        @Override
                        public void invoke(CollectionBookExcelDTO data, AnalysisContext context) {
                            rowIndex++;
                            try {
                                // 校验必填字段
                                if (!StringUtils.hasText(data.getBarcode())) {
                                    errors.add("第" + rowIndex + "行：条码不能为空");
                                    return;
                                }
                                if (!StringUtils.hasText(data.getBookName())) {
                                    errors.add("第" + rowIndex + "行：题名不能为空");
                                    return;
                                }

                                // 检查条码是否已存在
                                if (collectionBookMapper.countByBarcode(data.getBarcode()) > 0) {
                                    errors.add("第" + rowIndex + "行：条码已存在 - " + data.getBarcode());
                                    return;
                                }

                                // 转换为实体
                                CollectionBook book = convertFromExcelDTO(data);
                                book.setCreatedBy(createdBy);
                                book.setCreateTime(LocalDateTime.now());

                                successList.add(book);
                            } catch (Exception e) {
                                errors.add("第" + rowIndex + "行：数据格式错误 - " + e.getMessage());
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            // 批量插入成功的数据
                            if (!successList.isEmpty()) {
                                for (CollectionBook book : successList) {
                                    collectionBookMapper.insert(book);
                                }
                            }
                        }
                    }).sheet().doRead();

        } catch (IOException e) {
            log.error("导入馆藏图书失败", e);
            throw new BusinessException("读取Excel文件失败：" + e.getMessage());
        }

        result.put("successCount", successList.size());
        result.put("errorCount", errors.size());
        result.put("errors", errors);

        log.info("导入馆藏图书完成，成功：{}，失败：{}", successList.size(), errors.size());
        return result;
    }

    @Override
    @Log(module = "collection_book", operationType = "export")
    public void exportBooks(HttpServletResponse response, CollectionBookQueryRequest request) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(
                    "馆藏图书_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 查询数据
            request.setPageNum(1);
            request.setPageSize(Integer.MAX_VALUE); // 导出所有符合条件的数据
            Page<CollectionBookDTO> page = queryBooks(request);
            List<CollectionBookDTO> dtoList = page.getRecords();

            // 转换为 Excel DTO
            List<CollectionBookExcelDTO> excelList = dtoList.stream()
                    .map(this::convertToExcelDTO)
                    .collect(Collectors.toList());

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), CollectionBookExcelDTO.class)
                    .sheet("馆藏图书")
                    .doWrite(excelList);

            log.info("导出馆藏图书成功，数量：{}", excelList.size());
        } catch (IOException e) {
            log.error("导出馆藏图书失败", e);
            throw new BusinessException("导出Excel文件失败：" + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("馆藏图书导入模板", StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 创建示例数据
            List<CollectionBookExcelDTO> templateData = new ArrayList<>();
            CollectionBookExcelDTO example = new CollectionBookExcelDTO();
            example.setBarcode("TB000001");
            example.setBookName("示例书名");
            example.setAuthor("示例作者");
            example.setIsbn("9787000000001");
            example.setPublisher("示例出版社");
            example.setPublishYear("2024");
            example.setBranchLibrary("主馆");
            example.setCallNumber("A001");
            example.setPrice(new BigDecimal("39.80"));
            example.setBatch("2024-01");
            example.setIsStoredStr("是");
            example.setLibraryLocation("一楼");
            example.setShelfLocation("A区1排");
            example.setDuplicateFlagStr("否");
            example.setIsProblemStr("否");
            example.setProblemType("");
            example.setProblemReason("");
            templateData.add(example);

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), CollectionBookExcelDTO.class)
                    .sheet("馆藏图书导入模板")
                    .doWrite(templateData);

            log.info("下载馆藏图书导入模板成功");
        } catch (IOException e) {
            log.error("下载模板失败", e);
            throw new BusinessException("下载模板失败：" + e.getMessage());
        }
    }

    @Override
    @Log(module = "collection_book", operationType = "update")
    @Transactional
    public CollectionBookDTO markAsProblem(String barcode, String problemType, String problemReason, Long updatedBy) {
        CollectionBook book = collectionBookMapper.selectById(barcode);
        if (book == null) {
            throw new BusinessException("馆藏图书不存在，条码：" + barcode);
        }

        book.setIsProblem(1);
        book.setProblemType(problemType);
        book.setProblemReason(problemReason);
        book.setUpdatedBy(updatedBy);
        book.setUpdateTime(LocalDateTime.now());

        collectionBookMapper.updateById(book);

        log.info("将馆藏图书标记为问题图书，条码：{}", barcode);
        return convertToDTO(book);
    }

    @Override
    @Log(module = "collection_book", operationType = "update")
    @Transactional
    public CollectionBookDTO markAsNormal(String barcode, Long updatedBy) {
        CollectionBook book = collectionBookMapper.selectById(barcode);
        if (book == null) {
            throw new BusinessException("馆藏图书不存在，条码：" + barcode);
        }

        book.setIsProblem(0);
        book.setProblemType(null);
        book.setProblemReason(null);
        book.setUpdatedBy(updatedBy);
        book.setUpdateTime(LocalDateTime.now());

        collectionBookMapper.updateById(book);

        log.info("将问题图书恢复为正常馆藏，条码：{}", barcode);
        return convertToDTO(book);
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        long total = collectionBookMapper.selectCount(null);
        long normalCount = collectionBookMapper.countNormalBooks();
        long problemCount = collectionBookMapper.countProblemBooks();

        stats.put("total", total);
        stats.put("normalCount", normalCount);
        stats.put("problemCount", problemCount);

        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * Entity 转 DTO
     */
    private void selectListColumns(LambdaQueryWrapper<CollectionBook> queryWrapper, boolean includeProblemReason) {
        queryWrapper.select(CollectionBook.class,
                field -> includeProblemReason || !"problem_reason".equals(field.getColumn()));
    }

    private CollectionBookDTO convertToDTO(CollectionBook book) {
        CollectionBookDTO dto = new CollectionBookDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }

    /**
     * Excel DTO 转 Entity
     */
    private CollectionBook convertFromExcelDTO(CollectionBookExcelDTO excelDTO) {
        CollectionBook book = new CollectionBook();
        book.setBarcode(excelDTO.getBarcode());
        book.setBookName(excelDTO.getBookName());
        book.setAuthor(excelDTO.getAuthor());
        book.setIsbn(excelDTO.getIsbn());
        book.setPublisher(excelDTO.getPublisher());
        book.setPublishYear(excelDTO.getPublishYear());
        book.setBranchLibrary(excelDTO.getBranchLibrary());
        book.setCallNumber(excelDTO.getCallNumber());
        book.setPrice(excelDTO.getPrice());
        book.setBatch(excelDTO.getBatch());
        book.setLibraryLocation(excelDTO.getLibraryLocation());
        book.setShelfLocation(excelDTO.getShelfLocation());
        book.setProblemType(excelDTO.getProblemType());
        book.setProblemReason(excelDTO.getProblemReason());

        // 转换是否入库
        book.setIsStored("是".equals(excelDTO.getIsStoredStr()) ? 1 : 0);
        // 转换重复标记
        book.setDuplicateFlag("是".equals(excelDTO.getDuplicateFlagStr()) ? 1 : 0);
        // 转换是否问题图书
        book.setIsProblem("是".equals(excelDTO.getIsProblemStr()) ? 1 : 0);

        return book;
    }

    /**
     * DTO 转 Excel DTO
     */
    private CollectionBookExcelDTO convertToExcelDTO(CollectionBookDTO dto) {
        CollectionBookExcelDTO excelDTO = new CollectionBookExcelDTO();
        excelDTO.setBarcode(dto.getBarcode());
        excelDTO.setBookName(dto.getBookName());
        excelDTO.setAuthor(dto.getAuthor());
        excelDTO.setIsbn(dto.getIsbn());
        excelDTO.setPublisher(dto.getPublisher());
        excelDTO.setPublishYear(dto.getPublishYear());
        excelDTO.setBranchLibrary(dto.getBranchLibrary());
        excelDTO.setCallNumber(dto.getCallNumber());
        excelDTO.setPrice(dto.getPrice());
        excelDTO.setBatch(dto.getBatch());
        excelDTO.setLibraryLocation(dto.getLibraryLocation());
        excelDTO.setShelfLocation(dto.getShelfLocation());
        excelDTO.setProblemType(dto.getProblemType());
        excelDTO.setProblemReason(dto.getProblemReason());

        // 转换是否入库
        excelDTO.setIsStoredStr(dto.getIsStored() != null && dto.getIsStored() == 1 ? "是" : "否");
        // 转换重复标记
        excelDTO.setDuplicateFlagStr(dto.getDuplicateFlag() != null && dto.getDuplicateFlag() == 1 ? "是" : "否");
        // 转换是否问题图书
        excelDTO.setIsProblemStr(dto.getIsProblem() != null && dto.getIsProblem() == 1 ? "是" : "否");

        return excelDTO;
    }
    /**
     * is_problem + branch_library 的简单结构化筛选走定制 SQL，
     * 避免 MySQL 优化器误选到低效索引。
     */
    private Page<CollectionBookDTO> queryBooksByProblemAndBranch(CollectionBookQueryRequest request) {
        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1L : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10L : request.getPageSize();
        long offset = (pageNum - 1) * pageSize;

        long total = collectionBookMapper.countByProblemAndBranch(request.getIsProblem(), request.getBranchLibrary());
        List<CollectionBook> books = collectionBookMapper.selectPageByProblemAndBranch(
                request.getIsProblem(),
                request.getBranchLibrary(),
                offset,
                pageSize);

        Page<CollectionBookDTO> dtoPage = new Page<>(pageNum, pageSize, total);
        dtoPage.setRecords(books.stream().map(this::convertToDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    private boolean useProblemAndBranchFastPath(CollectionBookQueryRequest request) {
        return request != null
                && request.getIsProblem() != null
                && StringUtils.hasText(request.getBranchLibrary())
                && !StringUtils.hasText(request.getBarcode())
                && !StringUtils.hasText(request.getBookName())
                && !StringUtils.hasText(request.getAuthor())
                && !StringUtils.hasText(request.getIsbn())
                && !StringUtils.hasText(request.getPublisher())
                && !StringUtils.hasText(request.getPublishYear())
                && !StringUtils.hasText(request.getCallNumber())
                && !StringUtils.hasText(request.getBatch())
                && request.getIsStored() == null
                && !StringUtils.hasText(request.getLibraryLocation())
                && request.getDuplicateFlag() == null
                && !StringUtils.hasText(request.getProblemType());
    }
}
