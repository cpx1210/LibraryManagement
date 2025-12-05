package com.library.management.module.detection.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.management.common.exception.BusinessException;
import com.library.management.module.detection.dto.*;
import com.library.management.module.detection.entity.BooklistCheckDetail;
import com.library.management.module.detection.entity.BooklistCheckTask;
import com.library.management.module.detection.mapper.BooklistCheckDetailMapper;
import com.library.management.module.detection.mapper.BooklistCheckTaskMapper;
import com.library.management.module.detection.service.BooklistCheckService;
import com.library.management.module.detection.service.DetectionEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 书单检测服务实现类
 */
@Slf4j
@Service
public class BooklistCheckServiceImpl implements BooklistCheckService {

    @Resource
    private BooklistCheckTaskMapper taskMapper;

    @Resource
    private BooklistCheckDetailMapper detailMapper;

    @Resource
    private DetectionEngine detectionEngine;

    /**
     * 上传书单并创建检测任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BooklistUploadResponse uploadBooklist(MultipartFile file, Long userId, String userName) {
        log.info("用户 {} 上传书单文件：{}", userName, file.getOriginalFilename());

        // 1. 校验文件
        validateFile(file);

        // 2. 解析 Excel 文件
        List<BookItemDTO> books = parseExcel(file);

        if (books.isEmpty()) {
            throw new BusinessException("Excel 文件中没有有效的书目数据");
        }

        log.info("成功解析 {} 本书目", books.size());

        // 3. 生成任务名称
        String taskName = generateTaskName(userId, userName);

        // 4. 创建检测任务
        BooklistCheckTask task = BooklistCheckTask.builder()
                .taskName(taskName)
                .taskType("批量检测")
                .submittedBy(userId)
                .submitTime(LocalDateTime.now())
                .originalFilename(file.getOriginalFilename())
                .status("pending")
                .totalBooks(books.size())
                .sensitiveHits(0)
                .problemBookHits(0)
                .nonWhitelistPubs(0)
                .totalProblemBooks(0)
                .createdTime(LocalDateTime.now())
                .build();

        taskMapper.insert(task);

        log.info("创建检测任务成功：taskId={}, taskName={}", task.getTaskId(), taskName);

        // 5. 保存书目到明细表（初始状态为 pending）
        List<BooklistCheckDetail> details = books.stream()
                .map(book -> BooklistCheckDetail.builder()
                        .taskId(task.getTaskId())
                        .bookNumber(book.getBookNumber())
                        .isbn(book.getIsbn())
                        .bookName(book.getBookName())
                        .subtitle(book.getSubtitle())
                        .author1(book.getAuthor1())
                        .author2(book.getAuthor2())
                        .author(book.getAuthor()) // 合并的作者字段
                        .publishLocation(book.getPublishLocation())
                        .publisher(book.getPublisher())
                        .publishDate(book.getPublishDate())
                        .targetAudience(book.getTargetAudience())
                        .contentSummary(book.getContentSummary())
                        .classificationNumber(book.getClassificationNumber())
                        .language(book.getLanguage())
                        .hitSensitive(0)
                        .hitProblemBook(0)
                        .isWhitelistPublisher(0)
                        .checkStatus("pending")
                        .createdTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        // 批量插入
        batchInsertDetails(details);

        log.info("保存书目明细成功：{} 条", details.size());

        // 6. 异步执行检测
        executeDetection(task.getTaskId());

        // 7. 返回响应
        return BooklistUploadResponse.builder()
                .taskId(task.getTaskId())
                .taskName(taskName)
                .status("pending")
                .totalBooks(books.size())
                .message("上传成功，正在检测中...")
                .build();
    }

    /**
     * 执行检测任务（异步）
     */
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void executeDetection(Long taskId) {
        log.info("开始执行检测任务：taskId={}", taskId);

        try {
            // 1. 更新任务状态为 processing
            BooklistCheckTask task = taskMapper.selectById(taskId);
            if (task == null) {
                log.error("检测任务不存在：taskId={}", taskId);
                return;
            }

            task.setStatus("processing");
            task.setStartTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            // 2. 查询待检测的书目列表
            List<BooklistCheckDetail> details = detailMapper.selectByTaskId(taskId);

            log.info("待检测书目数量：{}", details.size());

            // 3. 将明细转换为 BookItemDTO
            List<BookItemDTO> books = details.stream()
                    .map(detail -> BookItemDTO.builder()
                            .bookNumber(detail.getBookNumber())
                            .bookName(detail.getBookName())
                            .subtitle(detail.getSubtitle())
                            .author1(detail.getAuthor1())
                            .author2(detail.getAuthor2())
                            .isbn(detail.getIsbn())
                            .publishLocation(detail.getPublishLocation())
                            .publisher(detail.getPublisher())
                            .publishDate(detail.getPublishDate())
                            .targetAudience(detail.getTargetAudience())
                            .contentSummary(detail.getContentSummary())
                            .classificationNumber(detail.getClassificationNumber())
                            .language(detail.getLanguage())
                            .build())
                    .collect(Collectors.toList());

            // 4. 批量检测
            List<DetectionResultDTO> results = detectionEngine.batchDetect(books);

            log.info("检测完成，结果数量：{}", results.size());

            // 5. 更新检测结果到明细表
            for (int i = 0; i < results.size() && i < details.size(); i++) {
                DetectionResultDTO result = results.get(i);
                BooklistCheckDetail detail = details.get(i);

                detail.setHitSensitive(Boolean.TRUE.equals(result.getHitSensitive()) ? 1 : 0);
                detail.setHitProblemBook(Boolean.TRUE.equals(result.getHitProblemBook()) ? 1 : 0);
                detail.setIsWhitelistPublisher(Boolean.TRUE.equals(result.getIsWhitelistPublisher()) ? 1 : 0);
                detail.setRiskLevel(result.getRiskLevel());

                // 保存敏感词详细信息（转为JSON字符串）
                if (result.getSensitiveHitDetails() != null && !result.getSensitiveHitDetails().isEmpty()) {
                    // 将详细信息转为 JSON 格式存储
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        detail.setSensitiveWords(objectMapper.writeValueAsString(result.getSensitiveHitDetails()));
                    } catch (Exception e) {
                        // 如果 JSON 序列化失败，使用简单格式
                        detail.setSensitiveWords(String.join(",", result.getSensitiveWords()));
                    }
                } else if (result.getSensitiveWords() != null && !result.getSensitiveWords().isEmpty()) {
                    detail.setSensitiveWords(String.join(",", result.getSensitiveWords()));
                }

                detail.setDetectionTime(LocalDateTime.now());
                detail.setCheckStatus("completed");
                detail.setUpdatedTime(LocalDateTime.now());

                detailMapper.updateById(detail);
            }

            // 6. 统计检测结果
            int sensitiveHits = (int) results.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getHitSensitive()))
                    .count();

            int problemBookHits = (int) results.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getHitProblemBook()))
                    .count();

            int nonWhitelistPubs = (int) results.stream()
                    .filter(r -> Boolean.FALSE.equals(r.getIsWhitelistPublisher()))
                    .count();

            int totalProblemBooks = (int) results.stream()
                    .filter(DetectionResultDTO::isProblemBook)
                    .count();

            // 7. 更新任务状态为 success
            task.setStatus("success");
            task.setEndTime(LocalDateTime.now());
            task.setSensitiveHits(sensitiveHits);
            task.setProblemBookHits(problemBookHits);
            task.setNonWhitelistPubs(nonWhitelistPubs);
            task.setTotalProblemBooks(totalProblemBooks);
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            log.info("检测任务完成：taskId={}, 敏感词命中={}, 问题书目命中={}, 非白名单={}, 总问题={}",
                    taskId, sensitiveHits, problemBookHits, nonWhitelistPubs, totalProblemBooks);

        } catch (Exception e) {
            log.error("检测任务失败：taskId={}, 错误：{}", taskId, e.getMessage(), e);

            // 更新任务状态为 failed
            BooklistCheckTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus("failed");
                task.setEndTime(LocalDateTime.now());
                task.setErrorMessage(e.getMessage());
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }
        }
    }

    /**
     * 分页查询检测任务列表
     */
    @Override
    public IPage<BooklistCheckTaskDTO> queryTasks(TaskQueryRequest request) {
        Page<BooklistCheckTask> page = new Page<>(request.getPageNum(), request.getPageSize());

        IPage<BooklistCheckTask> taskPage = taskMapper.selectTaskPage(
                page,
                request.getTaskName(),
                request.getStatus(),
                request.getSubmittedBy());

        // 转换为 DTO
        IPage<BooklistCheckTaskDTO> dtoPage = taskPage.convert(this::convertToDTO);

        return dtoPage;
    }

    /**
     * 查询检测任务详情
     */
    @Override
    public BooklistCheckTaskDTO getTaskDetail(Long taskId) {
        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("检测任务不存在");
        }

        return convertToDTO(task);
    }

    /**
     * 查询检测结果明细列表
     */
    @Override
    public List<CheckResultDetailDTO> getCheckDetails(Long taskId, String riskLevel) {
        List<BooklistCheckDetail> details = detailMapper.selectByTaskId(taskId);

        // 筛选风险等级
        if (StringUtils.hasText(riskLevel)) {
            details = details.stream()
                    .filter(d -> riskLevel.equals(d.getRiskLevel()))
                    .collect(Collectors.toList());
        }

        return details.stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 导出检测结果（Excel，带颜色标注）
     */
    @Override
    public void exportCheckResult(Long taskId, HttpServletResponse response) {
        log.info("导出检测结果：taskId={}", taskId);

        try {
            // 1. 查询任务信息
            BooklistCheckTask task = taskMapper.selectById(taskId);
            if (task == null) {
                throw new BusinessException("检测任务不存在");
            }

            // 2. 查询检测结果明细
            List<BooklistCheckDetail> details = detailMapper.selectByTaskId(taskId);

            // 3. 设置响应头
            String filename = URLEncoder.encode(task.getTaskName() + "_检测结果.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + filename);

            // 4. 使用 Apache POI 导出（带颜色标注）
            exportWithColors(details, response.getOutputStream());

            log.info("导出成功：{} 条记录", details.size());

        } catch (IOException e) {
            log.error("导出失败：{}", e.getMessage(), e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 下载检测模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        log.info("下载检测模板");

        try {
            String filename = URLEncoder.encode("书单检测模板.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + filename);

            // 创建模板
            createTemplate(response.getOutputStream());

        } catch (IOException e) {
            log.error("下载模板失败：{}", e.getMessage(), e);
            throw new BusinessException("下载模板失败：" + e.getMessage());
        }
    }

    /**
     * 删除检测任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        log.info("删除检测任务：taskId={}", taskId);

        // 1. 删除任务
        taskMapper.deleteById(taskId);

        // 2. 删除明细（级联删除由数据库外键处理，或手动删除）
        // 如果没有外键级联，需要手动删除
        // detailMapper.delete(new QueryWrapper<BooklistCheckDetail>().eq("task_id",
        // taskId));

        log.info("删除成功");
    }

    /**
     * 取消检测任务
     */
    @Override
    public void cancelTask(Long taskId) {
        log.info("取消检测任务：taskId={}", taskId);

        BooklistCheckTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("检测任务不存在");
        }

        if ("success".equals(task.getStatus()) || "failed".equals(task.getStatus())) {
            throw new BusinessException("任务已完成，无法取消");
        }

        task.setStatus("cancelled");
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        log.info("取消成功");
    }

    // ==================== 私有方法 ====================

    /**
     * 校验上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException("只支持 Excel 文件（.xlsx 或 .xls）");
        }

        // 限制文件大小（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过 50MB");
        }
    }

    /**
     * 解析 Excel 文件
     *
     * 新模板列结构：书号、题名、副题名、著者1、著者2、ISBN、出版地、出版社、出版日期、读者对象、内容简介、分类号、作品语种
     */
    private List<BookItemDTO> parseExcel(MultipartFile file) {
        List<BookItemDTO> books = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            int rowCount = 0;
            for (Row row : sheet) {
                // 跳过表头
                if (row.getRowNum() == 0) {
                    continue;
                }

                // 跳过空行
                if (isEmptyRow(row)) {
                    continue;
                }

                // 新模板列映射：
                // 0:书号 1:题名 2:副题名 3:著者1 4:著者2 5:ISBN 6:出版地 7:出版社 8:出版日期
                // 9:读者对象 10:内容简介 11:分类号 12:作品语种
                BookItemDTO book = BookItemDTO.builder()
                        .bookNumber(getCellValue(row.getCell(0)))
                        .bookName(getCellValue(row.getCell(1)))
                        .subtitle(getCellValue(row.getCell(2)))
                        .author1(getCellValue(row.getCell(3)))
                        .author2(getCellValue(row.getCell(4)))
                        .isbn(getCellValue(row.getCell(5)))
                        .publishLocation(getCellValue(row.getCell(6)))
                        .publisher(getCellValue(row.getCell(7)))
                        .publishDate(getCellValue(row.getCell(8)))
                        .targetAudience(getCellValue(row.getCell(9)))
                        .contentSummary(getCellValue(row.getCell(10)))
                        .classificationNumber(getCellValue(row.getCell(11)))
                        .language(getCellValue(row.getCell(12)))
                        .rowNumber(row.getRowNum() + 1)
                        .build();

                // 至少需要题名（书名）
                if (StringUtils.hasText(book.getBookName())) {
                    books.add(book);
                    rowCount++;
                }
            }

            workbook.close();

            log.info("解析 Excel 成功：共 {} 行有效数据", rowCount);

        } catch (Exception e) {
            log.error("解析 Excel 失败：{}", e.getMessage(), e);
            throw new BusinessException("解析 Excel 失败：" + e.getMessage());
        }

        return books;
    }

    /**
     * 判断是否为空行
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }

        // 检查前13列（新模板的所有列）
        for (int i = 0; i < 13; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.hasText(getCellValue(cell))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 处理数值类型（可能是 ISBN）
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // 转为字符串，去掉小数点
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 生成任务名称
     * 格式：提交人_日期_次数（例如：张三_20251026_01）
     */
    private String generateTaskName(Long userId, String userName) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查询今天已提交的任务数量
        int count = taskMapper.countTodayTasksByUser(userId);

        String sequence = String.format("%02d", count + 1);

        return userName + "_" + dateStr + "_" + sequence;
    }

    /**
     * 批量插入明细
     */
    private void batchInsertDetails(List<BooklistCheckDetail> details) {
        // 每次插入 500 条
        int batchSize = 500;
        for (int i = 0; i < details.size(); i += batchSize) {
            int end = Math.min(i + batchSize, details.size());
            List<BooklistCheckDetail> batch = details.subList(i, end);
            detailMapper.batchInsert(batch);
        }
    }

    /**
     * 转换为 DTO
     */
    private BooklistCheckTaskDTO convertToDTO(BooklistCheckTask task) {
        BooklistCheckTaskDTO dto = BooklistCheckTaskDTO.builder()
                .taskId(task.getTaskId())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .submittedBy(task.getSubmittedBy())
                .submitTime(task.getSubmitTime())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .originalFilename(task.getOriginalFilename())
                .status(task.getStatus())
                .statusText(getStatusText(task.getStatus()))
                .totalBooks(task.getTotalBooks())
                .sensitiveHits(task.getSensitiveHits())
                .problemBookHits(task.getProblemBookHits())
                .nonWhitelistPubs(task.getNonWhitelistPubs())
                .totalProblemBooks(task.getTotalProblemBooks())
                .errorMessage(task.getErrorMessage())
                .build();

        // 计算耗时
        if (task.getStartTime() != null && task.getEndTime() != null) {
            Duration duration = Duration.between(task.getStartTime(), task.getEndTime());
            dto.setDurationSeconds(duration.getSeconds());
        }

        return dto;
    }

    /**
     * 转换明细为 DTO
     */
    private CheckResultDetailDTO convertDetailToDTO(BooklistCheckDetail detail) {
        CheckResultDetailDTO dto = CheckResultDetailDTO.builder()
                .detailId(detail.getDetailId())
                .taskId(detail.getTaskId())
                .isbn(detail.getIsbn())
                .bookName(detail.getBookName())
                .author(detail.getAuthor())
                .publisher(detail.getPublisher())
                .hitSensitive(detail.getHitSensitive() == 1)
                .hitProblemBook(detail.getHitProblemBook() == 1)
                .isWhitelistPublisher(detail.getIsWhitelistPublisher() == 1)
                .riskLevel(detail.getRiskLevel())
                .riskLevelText(getRiskLevelText(detail.getRiskLevel()))
                .detectionTime(detail.getDetectionTime())
                .checkStatus(detail.getCheckStatus())
                .errorMessage(detail.getErrorMessage())
                .build();

        // 解析敏感词详细信息
        if (StringUtils.hasText(detail.getSensitiveWords())) {
            String sensitiveWordsJson = detail.getSensitiveWords();
            // 尝试解析为 JSON 数组（新格式）
            if (sensitiveWordsJson.startsWith("[")) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<SensitiveHitDetailDTO> hitDetails = objectMapper.readValue(
                            sensitiveWordsJson,
                            objectMapper.getTypeFactory().constructCollectionType(List.class,
                                    SensitiveHitDetailDTO.class));
                    dto.setSensitiveHitDetails(hitDetails);
                    // 同时生成简单格式的敏感词列表（用于兼容）
                    String simpleWords = hitDetails.stream()
                            .map(SensitiveHitDetailDTO::getKeyword)
                            .distinct()
                            .collect(Collectors.joining(","));
                    dto.setSensitiveWords(simpleWords);
                } catch (Exception e) {
                    log.warn("解析敏感词详细信息失败，使用原始格式：{}", e.getMessage());
                    dto.setSensitiveWords(sensitiveWordsJson);
                }
            } else {
                // 旧格式，直接使用
                dto.setSensitiveWords(sensitiveWordsJson);
            }
        }

        // 生成备注信息
        dto.setRemark(generateDetailRemark(dto));

        return dto;
    }

    /**
     * 生成明细备注信息（包含详细命中信息）
     */
    private String generateDetailRemark(CheckResultDetailDTO dto) {
        StringBuilder remark = new StringBuilder();

        // 敏感词详情
        if (Boolean.TRUE.equals(dto.getHitSensitive())) {
            remark.append("【敏感词】");
            if (dto.getSensitiveHitDetails() != null && !dto.getSensitiveHitDetails().isEmpty()) {
                // 使用详细信息
                List<String> detailTexts = dto.getSensitiveHitDetails().stream()
                        .map(SensitiveHitDetailDTO::toDisplayText)
                        .collect(Collectors.toList());
                remark.append(String.join("；", detailTexts));
            } else if (StringUtils.hasText(dto.getSensitiveWords())) {
                // 使用简单格式
                remark.append("命中敏感词：").append(dto.getSensitiveWords());
            }
        }

        // 问题书目
        if (Boolean.TRUE.equals(dto.getHitProblemBook())) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【问题书目】");
        }

        // 非白名单
        if (Boolean.FALSE.equals(dto.getIsWhitelistPublisher())) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【非白名单出版社】");
        }

        if (remark.length() == 0) {
            remark.append("无问题");
        }

        return remark.toString();
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        if (status == null) {
            return "";
        }

        switch (status) {
            case "pending":
                return "待处理";
            case "processing":
                return "处理中";
            case "success":
                return "成功";
            case "failed":
                return "失败";
            case "cancelled":
                return "已取消";
            default:
                return status;
        }
    }

    /**
     * 获取风险等级文本
     */
    private String getRiskLevelText(String riskLevel) {
        if (riskLevel == null) {
            return "";
        }

        switch (riskLevel) {
            case "high":
                return "高风险";
            case "medium":
                return "中风险";
            case "low":
                return "低风险";
            default:
                return riskLevel;
        }
    }

    /**
     * 使用 Apache POI 导出带颜色标注的 Excel
     */
    private void exportWithColors(List<BooklistCheckDetail> details, OutputStream out) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("检测结果");

        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle highRiskStyle = createHighRiskStyle(workbook); // 红色
        CellStyle mediumRiskStyle = createMediumRiskStyle(workbook); // 黄色
        CellStyle normalStyle = createNormalStyle(workbook);

        // 创建表头（包含所有新字段）
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "书号", "ISBN", "题名", "副题名", "著者1", "著者2",
                "出版地", "出版社", "出版日期", "读者对象", "内容简介",
                "分类号", "作品语种", "风险等级", "命中敏感词",
                "命中问题书目", "白名单出版社", "备注"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowNum = 1;
        for (BooklistCheckDetail detail : details) {
            Row row = sheet.createRow(rowNum++);

            // 根据风险等级选择样式
            CellStyle rowStyle;
            if ("high".equals(detail.getRiskLevel())) {
                rowStyle = highRiskStyle;
            } else if ("medium".equals(detail.getRiskLevel())) {
                rowStyle = mediumRiskStyle;
            } else {
                rowStyle = normalStyle;
            }

            // 填充单元格（按照新模板字段）
            int colIndex = 0;
            createCell(row, colIndex++, detail.getBookNumber(), rowStyle);
            createCell(row, colIndex++, detail.getIsbn(), rowStyle);
            createCell(row, colIndex++, detail.getBookName(), rowStyle);
            createCell(row, colIndex++, detail.getSubtitle(), rowStyle);
            createCell(row, colIndex++, detail.getAuthor1(), rowStyle);
            createCell(row, colIndex++, detail.getAuthor2(), rowStyle);
            createCell(row, colIndex++, detail.getPublishLocation(), rowStyle);
            createCell(row, colIndex++, detail.getPublisher(), rowStyle);
            createCell(row, colIndex++, detail.getPublishDate(), rowStyle);
            createCell(row, colIndex++, detail.getTargetAudience(), rowStyle);
            createCell(row, colIndex++, detail.getContentSummary(), rowStyle);
            createCell(row, colIndex++, detail.getClassificationNumber(), rowStyle);
            createCell(row, colIndex++, detail.getLanguage(), rowStyle);
            createCell(row, colIndex++, getRiskLevelText(detail.getRiskLevel()), rowStyle);
            createCell(row, colIndex++, detail.getHitSensitive() == 1 ? "是" : "否", rowStyle);
            createCell(row, colIndex++, detail.getHitProblemBook() == 1 ? "是" : "否", rowStyle);
            createCell(row, colIndex++, detail.getIsWhitelistPublisher() == 1 ? "是" : "否", rowStyle);

            // 生成备注
            String remark = generateRemark(detail);
            createCell(row, colIndex++, remark, rowStyle);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        // 写入输出流
        workbook.write(out);
        workbook.close();
    }

    /**
     * 创建单元格
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 生成备注信息
     */
    private String generateRemark(BooklistCheckDetail detail) {
        StringBuilder remark = new StringBuilder();

        if (detail.getHitSensitive() == 1) {
            remark.append("【敏感词】");
            if (StringUtils.hasText(detail.getSensitiveWords())) {
                remark.append("命中敏感词：").append(detail.getSensitiveWords());
            }
        }

        if (detail.getHitProblemBook() == 1) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【问题书目】");
        }

        if (detail.getIsWhitelistPublisher() == 0) {
            if (remark.length() > 0) {
                remark.append(" | ");
            }
            remark.append("【非白名单出版社】");
        }

        if (remark.length() == 0) {
            remark.append("无问题");
        }

        return remark.toString();
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    /**
     * 创建高风险样式（红色背景）
     */
    private CellStyle createHighRiskStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 红色背景
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 创建中风险样式（黄色背景）
     */
    private CellStyle createMediumRiskStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 黄色背景
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 创建普通样式
     */
    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 创建检测模板
     *
     * 新模板列结构：书号、题名、副题名、著者1、著者2、ISBN、出版地、出版社、出版日期、读者对象、内容简介、分类号、作品语种
     */
    private void createTemplate(OutputStream out) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("书单");

        // 创建表头样式
        CellStyle headerStyle = createHeaderStyle(workbook);

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "书号", "题名（必填）", "副题名", "著者1", "著者2", "ISBN",
                "出版地", "出版社", "出版日期", "读者对象", "内容简介", "分类号", "作品语种"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 添加示例数据
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("BK001");
        row1.createCell(1).setCellValue("深入理解计算机系统");
        row1.createCell(2).setCellValue("原书第3版");
        row1.createCell(3).setCellValue("[美] Randal E. Bryant");
        row1.createCell(4).setCellValue("[美] David R. O'Hallaron");
        row1.createCell(5).setCellValue("9787111681526");
        row1.createCell(6).setCellValue("北京");
        row1.createCell(7).setCellValue("机械工业出版社");
        row1.createCell(8).setCellValue("2021");
        row1.createCell(9).setCellValue("计算机专业学生");
        row1.createCell(10).setCellValue("经典的计算机系统教材");
        row1.createCell(11).setCellValue("TP3");
        row1.createCell(12).setCellValue("英文");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("BK002");
        row2.createCell(1).setCellValue("Python编程：从入门到实践");
        row2.createCell(2).setCellValue("第2版");
        row2.createCell(3).setCellValue("[美] Eric Matthes");
        row2.createCell(4).setCellValue("");
        row2.createCell(5).setCellValue("9787115545312");
        row2.createCell(6).setCellValue("北京");
        row2.createCell(7).setCellValue("人民邮电出版社");
        row2.createCell(8).setCellValue("2020");
        row2.createCell(9).setCellValue("编程初学者");
        row2.createCell(10).setCellValue("适合零基础学习Python");
        row2.createCell(11).setCellValue("TP311.56");
        row2.createCell(12).setCellValue("英文");

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1500);
        }

        // 写入输出流
        workbook.write(out);
        workbook.close();
    }
}