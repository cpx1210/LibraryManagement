package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书目信息 DTO（用于从 Excel 读取）
 *
 * 对应 input_data.xlsx 的列结构：
 * 书号、题名、副题名、著者1、著者2、ISBN、出版地、出版社、出版日期、读者对象、内容简介、分类号、作品语种
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookItemDTO {

    /**
     * 书号
     */
    private String bookNumber;

    /**
     * 题名（书名）
     */
    private String bookName;

    /**
     * 副题名
     */
    private String subtitle;

    /**
     * 著者1（第一作者）
     */
    private String author1;

    /**
     * 著者2（第二作者）
     */
    private String author2;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 出版地
     */
    private String publishLocation;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版日期
     */
    private String publishDate;

    /**
     * 读者对象
     */
    private String targetAudience;

    /**
     * 内容简介
     */
    private String contentSummary;

    /**
     * 分类号
     */
    private String classificationNumber;

    /**
     * 作品语种
     */
    private String language;

    /**
     * 行号（Excel 中的行号，用于错误提示）
     */
    private Integer rowNumber;

    /**
     * 获取作者信息（合并著者1和著者2）
     * 用于向后兼容和检测逻辑
     */
    public String getAuthor() {
        StringBuilder author = new StringBuilder();
        if (author1 != null && !author1.trim().isEmpty()) {
            author.append(author1.trim());
        }
        if (author2 != null && !author2.trim().isEmpty()) {
            if (author.length() > 0) {
                author.append("; ");
            }
            author.append(author2.trim());
        }
        return author.length() > 0 ? author.toString() : null;
    }

    /**
     * 获取出版年份（从出版日期中提取）
     * 用于向后兼容
     */
    public String getPublishYear() {
        if (publishDate != null && !publishDate.trim().isEmpty()) {
            // 尝试提取年份（前4位数字）
            String dateStr = publishDate.trim();
            if (dateStr.length() >= 4) {
                return dateStr.substring(0, 4);
            }
        }
        return null;
    }
}
