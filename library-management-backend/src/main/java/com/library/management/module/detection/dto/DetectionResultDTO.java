package com.library.management.module.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单本书检测结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResultDTO {

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 书名
     */
    private String bookName;

    /**
     * 副题名
     */
    private String subtitle;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版地
     */
    private String publishLocation;

    /**
     * 读者对象
     */
    private String targetAudience;

    /**
     * 内容简介
     */
    private String contentSummary;

    /**
     * 是否命中敏感词
     */
    private Boolean hitSensitive;

    /**
     * 命中的敏感词列表（简单关键词列表，用于兼容）
     */
    private List<String> sensitiveWords;

    /**
     * 命中的敏感词详细信息列表
     * 包含：字段名、关键词、原因等详细信息
     */
    private List<SensitiveHitDetailDTO> sensitiveHitDetails;

    /**
     * 敏感词最高风险等级
     * 1 - 低风险
     * 2 - 中风险
     * 3 - 高风险
     */
    private Integer maxSensitiveRiskLevel;

    /**
     * 是否命中问题书目
     */
    private Boolean hitProblemBook;

    /**
     * 命中的问题书目信息
     */
    private String problemBookInfo;

    /**
     * 是否为白名单出版社
     */
    private Boolean isWhitelistPublisher;

    /**
     * 综合风险等级
     * high - 高风险（命中敏感词）
     * medium - 中风险（命中问题书目）
     * low - 低风险（非白名单出版社）
     */
    private String riskLevel;

    /**
     * 检测备注信息
     */
    private String remark;

    /**
     * 判断是否为问题书目（命中敏感词或问题书目）
     *
     * @return 是否为问题书目
     */
    public boolean isProblemBook() {
        return Boolean.TRUE.equals(hitSensitive) || Boolean.TRUE.equals(hitProblemBook);
    }

    /**
     * 计算综合风险等级
     */
    public void calculateRiskLevel() {
        if (Boolean.TRUE.equals(hitSensitive)) {
            this.riskLevel = "high";
        } else if (Boolean.TRUE.equals(hitProblemBook)) {
            this.riskLevel = "medium";
        } else if (Boolean.FALSE.equals(isWhitelistPublisher)) {
            this.riskLevel = "low";
        } else {
            this.riskLevel = "low";
        }
    }
}
