package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书目信息 DTO（用于从 Excel 读取）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookItemDTO {

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版年份
     */
    private String publishYear;

    /**
     * 行号（Excel 中的行号，用于错误提示）
     */
    private Integer rowNumber;
}
