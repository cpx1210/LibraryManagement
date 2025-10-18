package com.library.management.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Excel 工具类（基于 EasyExcel）
 *
 * 功能说明：
 * 1. Excel 文件读取（导入）
 * 2. Excel 文件写入（导出）
 * 3. 支持自定义单元格样式（颜色标注）
 * 4. 支持错误行处理
 * 5. 支持模板文件下载
 *
 * @author Library Management System
 * @since 2025-10-14
 */
@Slf4j
public class ExcelUtil {

    /**
     * 读取 Excel 文件，转换为对象列表
     *
     * @param file      上传的 Excel 文件
     * @param clazz     目标对象类型
     * @param <T>       泛型
     * @return          对象列表
     */
    public static <T> List<T> read(MultipartFile file, Class<T> clazz) {
        List<T> dataList = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), clazz, new AnalysisEventListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    // 每读取一行数据，添加到列表
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 读取完成，共 {} 行数据", dataList.size());
                }
            }).sheet().doRead();
        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            throw new RuntimeException("读取 Excel 文件失败: " + e.getMessage());
        }
        return dataList;
    }

    /**
     * 读取 Excel 文件，支持自定义数据处理（逐行处理，节省内存）
     *
     * @param file      上传的 Excel 文件
     * @param clazz     目标对象类型
     * @param consumer  数据消费者（每读取一行调用一次）
     * @param <T>       泛型
     */
    public static <T> void readWithConsumer(MultipartFile file, Class<T> clazz,
                                             java.util.function.Consumer<T> consumer) {
        try {
            EasyExcel.read(file.getInputStream(), clazz, new AnalysisEventListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    // 每读取一行，调用消费者处理
                    consumer.accept(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 读取完成");
                }
            }).sheet().doRead();
        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            throw new RuntimeException("读取 Excel 文件失败: " + e.getMessage());
        }
    }

    /**
     * 写入 Excel 文件到 HTTP 响应（导出）
     *
     * @param response  HTTP 响应对象
     * @param dataList  数据列表
     * @param clazz     数据对象类型
     * @param fileName  文件名（不含扩展名）
     * @param <T>       泛型
     */
    public static <T> void write(HttpServletResponse response, List<T> dataList,
                                  Class<T> clazz, String fileName) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet("Sheet1")
                    .doWrite(dataList);
        } catch (IOException e) {
            log.error("导出 Excel 文件失败", e);
            throw new RuntimeException("导出 Excel 文件失败: " + e.getMessage());
        }
    }

    /**
     * 写入 Excel 文件，支持自定义样式（颜色标注）
     *
     * 使用场景：书单检测结果导出，命中敏感词标红色，命中问题书目标黄色
     *
     * @param response      HTTP 响应对象
     * @param dataList      数据列表
     * @param clazz         数据对象类型
     * @param fileName      文件名（不含扩展名）
     * @param colorFunction 颜色判断函数（返回 IndexedColors，null 表示默认）
     * @param <T>           泛型
     */
    public static <T> void writeWithColor(HttpServletResponse response, List<T> dataList,
                                           Class<T> clazz, String fileName,
                                           Function<T, IndexedColors> colorFunction) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 写入 Excel，注册颜色样式处理器
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new ColorCellWriteHandler<>(dataList, colorFunction))
                    .sheet("Sheet1")
                    .doWrite(dataList);
        } catch (IOException e) {
            log.error("导出 Excel 文件失败", e);
            throw new RuntimeException("导出 Excel 文件失败: " + e.getMessage());
        }
    }

    /**
     * 下载模板文件
     *
     * @param response      HTTP 响应对象
     * @param templatePath  模板文件路径（相对于 resources 目录）
     * @param fileName      下载后的文件名（不含扩展名）
     */
    public static void downloadTemplate(HttpServletResponse response,
                                         String templatePath, String fileName) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 读取模板文件
            ClassPathResource resource = new ClassPathResource(templatePath);
            try (InputStream inputStream = resource.getInputStream()) {
                // 复制到响应输出流
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, len);
                }
                response.getOutputStream().flush();
            }
        } catch (IOException e) {
            log.error("下载模板文件失败", e);
            throw new RuntimeException("下载模板文件失败: " + e.getMessage());
        }
    }

    /**
     * 自定义单元格样式处理器（用于颜色标注）
     *
     * @param <T> 数据类型
     */
    private static class ColorCellWriteHandler<T> implements CellWriteHandler {
        private final List<T> dataList;
        private final Function<T, IndexedColors> colorFunction;

        public ColorCellWriteHandler(List<T> dataList, Function<T, IndexedColors> colorFunction) {
            this.dataList = dataList;
            this.colorFunction = colorFunction;
        }

        @Override
        public void afterCellDispose(WriteSheetHolder writeSheetHolder,
                                      WriteTableHolder writeTableHolder,
                                      List<WriteCellData<?>> cellDataList,
                                      Cell cell,
                                      Head head,
                                      Integer relativeRowIndex,
                                      Boolean isHead) {
            // 跳过表头
            if (Boolean.TRUE.equals(isHead) || relativeRowIndex == null
                    || relativeRowIndex >= dataList.size()) {
                return;
            }

            // 获取当前行数据
            T rowData = dataList.get(relativeRowIndex);

            // 判断是否需要设置颜色
            IndexedColors color = colorFunction.apply(rowData);
            if (color != null) {
                // 创建单元格样式
                Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.cloneStyleFrom(cell.getCellStyle());

                // 设置背景颜色
                cellStyle.setFillForegroundColor(color.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // 应用样式
                cell.setCellStyle(cellStyle);
            }
        }
    }
}
