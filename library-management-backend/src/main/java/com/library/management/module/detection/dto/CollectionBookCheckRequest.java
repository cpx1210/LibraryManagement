package com.library.management.module.detection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 馆藏书目检测请求
 */
@Data
@Schema(description = "馆藏书目检测请求")
public class CollectionBookCheckRequest {

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "条码")
    private String barcode;

    @Schema(description = "题名")
    private String bookName;

    @Schema(description = "著者")
    private String author;

    @Schema(description = "ISBN")
    private String isbn;

    @Schema(description = "出版社")
    private String publisher;

    @Schema(description = "出版年")
    private String publishYear;

    @Schema(description = "分馆")
    private String branchLibrary;

    @Schema(description = "索书号")
    private String callNumber;

    @Schema(description = "批次")
    private String batch;

    @Schema(description = "是否入库")
    private Integer isStored;

    @Schema(description = "馆藏院舍")
    private String libraryLocation;

    @Schema(description = "重复标记")
    private Integer duplicateFlag;

    @Schema(description = "是否问题图书")
    private Integer isProblem;

    @Schema(description = "问题类型")
    private String problemType;
}
