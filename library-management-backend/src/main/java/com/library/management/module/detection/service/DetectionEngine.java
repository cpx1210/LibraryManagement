package com.library.management.module.detection.service;

import com.library.management.module.detection.dto.BookItemDTO;
import com.library.management.module.detection.dto.DetectionResultDTO;

import java.util.List;

/**
 * 检测引擎接口
 *
 * 核心功能：
 * 1. 敏感词检测（使用 PostgreSQL ILIKE 模糊匹配）
 * 2. 问题书目检测（ISBN 精确匹配 + 书名相似度匹配）
 * 3. 出版社白名单检测（精确匹配）
 * 4. 综合检测（整合三种检测结果）
 */
public interface DetectionEngine {

    /**
     * 检测单本书
     *
     * @param book 书目信息
     * @return 检测结果
     */
    DetectionResultDTO detectBook(BookItemDTO book);

    /**
     * 批量检测书目
     *
     * @param books 书目列表
     * @return 检测结果列表
     */
    List<DetectionResultDTO> batchDetect(List<BookItemDTO> books);
}
