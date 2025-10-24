package com.library.management.module.problembook.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 问题书目修改请求
 * 用于更新问题书目信息
 *
 * @author Library Management System
 * @since 2025-10-22
 */
@Data
public class ProblemBookUpdateRequest {

    /**
     * 书目ID（必填，用于指定要修改的问题书目）
     */
    @NotNull(message = "书目ID不能为空")
    private Long bookId;

    /**
     * 书名（可选）
     */
    @Size(min = 1, max = 500, message = "书名长度必须为1-500个字符")
    private String bookName;

    /**
     * 作者（可选）
     */
    @Size(max = 200, message = "作者名称最多200个字符")
    private String author;

    /**
     * ISBN 编号（可选）
     */
    @Size(max = 20, message = "ISBN编号最多20个字符")
    private String isbn;

    /**
     * 出版社（可选）
     */
    @Size(max = 200, message = "出版社名称最多200个字符")
    private String publisher;

    /**
     * 出版年份（可选，格式：YYYY）
     */
    @Pattern(regexp = "^\\d{4}$|^$", message = "出版年份格式必须为4位数字（如：2023）")
    private String publishYear;

    /**
     * 问题类型（可选）
     */
    @Size(max = 100, message = "问题类型最多100个字符")
    private String problemType;

    /**
     * 来源（可选）
     */
    @Size(max = 500, message = "来源说明最多500个字符")
    private String source;
}
