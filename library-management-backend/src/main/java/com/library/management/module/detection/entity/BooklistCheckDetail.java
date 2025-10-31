package com.library.management.module.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 书单检测结果明细实体类
 * 对应数据库表：booklist_check_detail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("booklist_check_detail")
public class BooklistCheckDetail {

    /**
     * 明细ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long detailId;

    /**
     * 任务ID（外键）
     */
    private Long taskId;

    /**
     * 书号
     */
    private String bookNumber;

    /**
     * 书名（题名）
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
     * 作者（合并字段，用于向后兼容）
     */
    private String author;

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
     * 是否命中敏感词
     * 0 - 未命中
     * 1 - 命中
     */
    private Integer hitSensitive;

    /**
     * 是否命中问题书目
     * 0 - 未命中
     * 1 - 命中
     */
    private Integer hitProblemBook;

    /**
     * 是否为白名单出版社
     * 0 - 否
     * 1 - 是
     */
    private Integer isWhitelistPublisher;

    /**
     * 风险等级
     * high - 高风险（命中敏感词）
     * medium - 中风险（命中问题书目）
     * low - 低风险（非白名单出版社）
     */
    private String riskLevel;

    /**
     * 命中的敏感词详情
     * JSON 格式存储敏感词列表
     * 例如：["敏感词1", "敏感词2"]
     */
    private String sensitiveWords;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 检测状态
     * pending - 待检测
     * completed - 已完成
     * error - 检测失败
     */
    private String checkStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
