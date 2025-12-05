-- =====================================================
-- 馆藏图书表结构 (collection_books)
-- =====================================================
-- 功能：存储图书馆馆藏图书信息，包括正常馆藏和问题图书
-- 创建日期：2025-12-05
-- =====================================================

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS collection_books;

-- =====================================================
-- 创建馆藏图书表
-- =====================================================
CREATE TABLE collection_books (
    -- 主键：条码（图书唯一标识）
    barcode VARCHAR(50) PRIMARY KEY COMMENT '条码（主键，图书唯一标识）',
    
    -- 基本信息
    book_name VARCHAR(500) NOT NULL COMMENT '题名（书名）',
    author VARCHAR(200) COMMENT '著者（作者）',
    isbn VARCHAR(20) COMMENT 'ISBN',
    publisher VARCHAR(200) COMMENT '出版社',
    publish_year VARCHAR(10) COMMENT '出版年',
    
    -- 馆藏信息
    branch_library VARCHAR(100) COMMENT '分馆',
    call_number VARCHAR(100) COMMENT '索书号',
    price DECIMAL(10, 2) COMMENT '单价',
    batch VARCHAR(50) COMMENT '批次',
    
    -- 入库状态
    is_stored TINYINT(1) DEFAULT 1 COMMENT '是否入库：0-否，1-是',
    
    -- 馆藏位置（分为两个字段）
    library_location VARCHAR(100) COMMENT '馆藏院舍',
    shelf_location VARCHAR(100) COMMENT '书架位置',
    
    -- 重复标记
    duplicate_flag TINYINT(1) DEFAULT 0 COMMENT '重复标记：0-否，1-是',
    
    -- 问题图书标记
    is_problem TINYINT(1) DEFAULT 0 COMMENT '是否问题图书：0-正常馆藏，1-问题图书',
    problem_type VARCHAR(100) COMMENT '问题类型',
    problem_reason TEXT COMMENT '问题原因/备注',
    
    -- 审计字段
    created_by BIGINT COMMENT '创建人用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人用户ID',
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    CONSTRAINT fk_cb_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_cb_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='馆藏图书表';

-- =====================================================
-- 创建索引
-- =====================================================
-- ISBN 索引（用于检索）
CREATE INDEX idx_cb_isbn ON collection_books(isbn);

-- 书名索引（支持模糊查询）
CREATE INDEX idx_cb_book_name ON collection_books(book_name(255));

-- 作者索引
CREATE INDEX idx_cb_author ON collection_books(author);

-- 出版社索引
CREATE INDEX idx_cb_publisher ON collection_books(publisher);

-- 分馆索引
CREATE INDEX idx_cb_branch_library ON collection_books(branch_library);

-- 索书号索引
CREATE INDEX idx_cb_call_number ON collection_books(call_number);

-- 是否问题图书索引（用于区分馆藏图书和问题图书）
CREATE INDEX idx_cb_is_problem ON collection_books(is_problem);

-- 批次索引
CREATE INDEX idx_cb_batch ON collection_books(batch);

-- 入库状态索引
CREATE INDEX idx_cb_is_stored ON collection_books(is_stored);

-- =====================================================
-- 说明
-- =====================================================
-- 1. 条码(barcode)作为主键，确保每本书的唯一性
-- 2. is_problem 字段区分正常馆藏(0)和问题图书(1)
-- 3. 馆藏位置分为：library_location(馆藏院舍) 和 shelf_location(书架位置)
-- 4. 支持 Excel 批量导入导出
-- =====================================================

