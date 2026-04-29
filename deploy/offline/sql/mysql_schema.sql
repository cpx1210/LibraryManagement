-- =====================================================
-- Library Problem Books Management System - Database Schema (MySQL Version)
-- =====================================================
-- Database: MySQL 8.0+
-- Encoding: UTF-8
-- Created: 2025-11-06
-- Description: Creates all database tables from scratch for MySQL
-- =====================================================

-- Set character encoding
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =====================================================
-- 0. Create Database (if not exists)
-- =====================================================
CREATE DATABASE IF NOT EXISTS library_management 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE library_management;

-- =====================================================
-- 1. sys_user
-- =====================================================
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID（主键）',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名（唯一，用于登录）',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
    employee_id VARCHAR(50) UNIQUE COMMENT '员工ID（唯一）',
    role VARCHAR(20) NOT NULL COMMENT '用户角色：admin, user',
    department VARCHAR(100) COMMENT '部门',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    is_active TINYINT(1) DEFAULT 1 COMMENT '账号状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    CHECK (role IN ('admin', 'user')),
    CHECK (is_active IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_employee_id ON sys_user(employee_id);
CREATE INDEX idx_user_role ON sys_user(role);

-- =====================================================
-- 2. sensitive_categories
-- =====================================================
DROP TABLE IF EXISTS sensitive_categories;

CREATE TABLE sensitive_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID（主键）',
    category_name VARCHAR(50) UNIQUE NOT NULL COMMENT '分类名称：作者、书名、出版社、关键词',
    description VARCHAR(200) COMMENT '分类描述',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID（FK -> sys_user.user_id）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人用户ID（FK -> sys_user.user_id）',
    update_time DATETIME COMMENT '更新时间',
    CONSTRAINT fk_sc_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_sc_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词分类表';

CREATE INDEX idx_sc_category_name ON sensitive_categories(category_name);

-- =====================================================
-- 3. sensitive_words
-- =====================================================
DROP TABLE IF EXISTS sensitive_words;

CREATE TABLE sensitive_words (
    word_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '词ID（主键）',
    category_id BIGINT NOT NULL COMMENT '分类ID（FK -> sensitive_categories.category_id）',
    keyword VARCHAR(100) NOT NULL COMMENT '敏感关键词（最大100字符）',
    match_type TINYINT NOT NULL DEFAULT 1 COMMENT '匹配类型：0-精确，1-模糊，2-正则',
    risk_level TINYINT NOT NULL DEFAULT 2 COMMENT '风险等级：1-低，2-中，3-高',
    detection_type VARCHAR(20) NOT NULL DEFAULT '关键词' COMMENT '检测类型：关键词-全局，书名-仅书名，作者-仅作者',
    alert_message VARCHAR(200) COMMENT '警告信息：匹配到关键词时的警告消息',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用（默认启用）',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID（FK -> sys_user.user_id）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人用户ID（FK -> sys_user.user_id）',
    update_time DATETIME COMMENT '更新时间',
    CONSTRAINT fk_sw_category FOREIGN KEY (category_id) REFERENCES sensitive_categories(category_id),
    CONSTRAINT fk_sw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_sw_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id),
    CHECK (match_type IN (0, 1, 2)),
    CHECK (risk_level IN (1, 2, 3)),
    CHECK (detection_type IN ('关键词', '书名', '作者'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 为关键词字段创建全文索引（支持中文搜索）
CREATE FULLTEXT INDEX idx_sw_keyword_fulltext ON sensitive_words(keyword) WITH PARSER ngram;
CREATE INDEX idx_sw_keyword ON sensitive_words(keyword);
CREATE INDEX idx_sw_category_id ON sensitive_words(category_id);
CREATE INDEX idx_sw_detection_type ON sensitive_words(detection_type);
CREATE INDEX idx_sw_is_active ON sensitive_words(is_active);

-- =====================================================
-- 4. problem_books
-- =====================================================
DROP TABLE IF EXISTS problem_books;

CREATE TABLE problem_books (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图书ID（主键）',
    book_name VARCHAR(500) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    isbn VARCHAR(20) COMMENT 'ISBN',
    publisher VARCHAR(200) COMMENT '出版社',
    publish_year VARCHAR(4) COMMENT '出版年份（格式：YYYY）',
    problem_type VARCHAR(100) COMMENT '问题类型',
    source VARCHAR(255) COMMENT '来源',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID（FK -> sys_user.user_id）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人用户ID（FK -> sys_user.user_id）',
    update_time DATETIME COMMENT '更新时间',
    CONSTRAINT fk_pb_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_pb_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题图书表';

CREATE INDEX idx_pb_isbn ON problem_books(isbn);
-- 为书名创建全文索引（支持中文搜索）
CREATE FULLTEXT INDEX idx_pb_book_name_fulltext ON problem_books(book_name) WITH PARSER ngram;
CREATE INDEX idx_pb_book_name ON problem_books(book_name(255));
CREATE INDEX idx_pb_author ON problem_books(author);
CREATE INDEX idx_pb_publisher ON problem_books(publisher);

-- =====================================================
-- 5. publisher_whitelist
-- =====================================================
DROP TABLE IF EXISTS publisher_whitelist;

CREATE TABLE publisher_whitelist (
    publisher_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出版社ID（主键）',
    publisher_name VARCHAR(200) UNIQUE NOT NULL COMMENT '出版社名称（唯一）',
    years BIGINT COMMENT '年份批次',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用（默认启用）',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID（FK -> sys_user.user_id）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_pw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出版社白名单表';

CREATE INDEX idx_pw_name ON publisher_whitelist(publisher_name);
CREATE INDEX idx_pw_is_active ON publisher_whitelist(is_active);

-- =====================================================
-- 6. purchased_problem_books
-- =====================================================
DROP TABLE IF EXISTS purchased_problem_books;

CREATE TABLE purchased_problem_books (
    asset_code BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资产编码（主键，自增）',
    system_id VARCHAR(20) COMMENT '系统ID：system_1-Aleph，system_2-超星',
    isbn VARCHAR(20) COMMENT 'ISBN',
    book_name VARCHAR(500) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    publisher VARCHAR(200) COMMENT '出版社',
    purchase_date DATE COMMENT '采购日期',
    location VARCHAR(100) COMMENT '存放位置',
    problem_reason TEXT COMMENT '问题原因',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending, processing, processed, reinstated',
    handler_id BIGINT COMMENT '处理人用户ID（FK -> sys_user.user_id）',
    handler_date DATETIME COMMENT '处理日期',
    CONSTRAINT fk_ppb_handler FOREIGN KEY (handler_id) REFERENCES sys_user(user_id),
    CHECK (system_id IN ('system_1', 'system_2')),
    CHECK (status IN ('pending', 'processing', 'processed', 'reinstated'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='已采购问题图书表';

CREATE INDEX idx_ppb_isbn ON purchased_problem_books(isbn);
CREATE INDEX idx_ppb_status ON purchased_problem_books(status);
CREATE INDEX idx_ppb_system_id ON purchased_problem_books(system_id);

-- =====================================================
-- 7. operation_log
-- =====================================================
DROP TABLE IF EXISTS operation_log;

CREATE TABLE operation_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID（主键）',
    module VARCHAR(50) NOT NULL COMMENT '模块（例如：purchased_problem_books）',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：create, update, delete',
    target_id BIGINT COMMENT '目标记录ID',
    old_value TEXT COMMENT '旧值（JSON格式）',
    new_value TEXT COMMENT '新值（JSON格式）',
    operated_by BIGINT NOT NULL COMMENT '操作人用户ID（FK -> sys_user.user_id）',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    CONSTRAINT fk_log_operator FOREIGN KEY (operated_by) REFERENCES sys_user(user_id),
    CHECK (operation_type IN ('create', 'update', 'delete'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

CREATE INDEX idx_log_module ON operation_log(module);
CREATE INDEX idx_log_operation_type ON operation_log(operation_type);
CREATE INDEX idx_log_operated_by ON operation_log(operated_by);
CREATE INDEX idx_log_operation_time ON operation_log(operation_time);

-- =====================================================
-- 8. booklist_check_task
-- =====================================================
DROP TABLE IF EXISTS booklist_check_task;

CREATE TABLE booklist_check_task (
    task_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID（主键）',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称（格式：提交人_日期_序号）',
    task_type VARCHAR(50) COMMENT '任务类型：批量检测、单本检测',
    submitted_by BIGINT NOT NULL COMMENT '提交人用户ID（FK -> sys_user.user_id）',
    submitter_name VARCHAR(100) COMMENT '上传人姓名',
    submitter_department VARCHAR(100) COMMENT '上传人部门',
    submitter_email VARCHAR(100) COMMENT '上传人邮箱',
    submitter_employee_no VARCHAR(50) COMMENT '上传人工号',
    submitter_mobile VARCHAR(20) COMMENT '上传人手机号',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    original_filename VARCHAR(500) COMMENT '原始文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    result_file_path VARCHAR(500) COMMENT '结果文件路径',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending, processing, success, failed, cancelled',
    error_message TEXT COMMENT '错误信息',
    total_books INT DEFAULT 0 COMMENT '总图书数',
    processed_books INT DEFAULT 0 COMMENT '已处理书目数',
    current_batch INT DEFAULT 0 COMMENT '当前批次',
    total_batches INT DEFAULT 0 COMMENT '总批次数',
    sensitive_hits INT DEFAULT 0 COMMENT '敏感词命中数',
    problem_book_hits INT DEFAULT 0 COMMENT '问题图书命中数',
    non_whitelist_pubs INT DEFAULT 0 COMMENT '非白名单出版社数',
    total_problem_books INT DEFAULT 0 COMMENT '总问题图书数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    CONSTRAINT fk_task_submitter FOREIGN KEY (submitted_by) REFERENCES sys_user(user_id),
    CHECK (status IN ('pending', 'processing', 'success', 'failed', 'cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书目检测任务表';

CREATE INDEX idx_task_submitted_by ON booklist_check_task(submitted_by);
CREATE INDEX idx_task_submit_time ON booklist_check_task(submit_time);
CREATE INDEX idx_task_status ON booklist_check_task(status);

-- =====================================================
-- 9. booklist_check_detail
-- =====================================================
DROP TABLE IF EXISTS booklist_check_detail;

CREATE TABLE booklist_check_detail (
    detail_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '详情ID（主键）',
    task_id BIGINT NOT NULL COMMENT '任务ID（FK -> booklist_check_task.task_id）',
    book_number VARCHAR(50) COMMENT '图书编号',
    book_name VARCHAR(500) COMMENT '书名（标题）',
    subtitle VARCHAR(200) COMMENT '副标题',
    author1 VARCHAR(100) COMMENT '作者1（第一作者）',
    author2 VARCHAR(100) COMMENT '作者2（第二作者）',
    author VARCHAR(100) COMMENT '合并作者（用于兼容）',
    isbn VARCHAR(20) COMMENT 'ISBN',
    publish_location VARCHAR(100) COMMENT '出版地',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date VARCHAR(50) COMMENT '出版日期',
    target_audience VARCHAR(100) COMMENT '目标读者',
    content_summary TEXT COMMENT '内容摘要',
    classification_number VARCHAR(50) COMMENT '分类号',
    language VARCHAR(50) COMMENT '语言',
    hit_sensitive TINYINT DEFAULT 0 COMMENT '命中敏感词：1-是，0-否',
    hit_problem_book TINYINT DEFAULT 0 COMMENT '命中问题图书：1-是，0-否',
    is_whitelist_publisher TINYINT DEFAULT 1 COMMENT '是否白名单出版社：1-是，0-否',
    risk_level VARCHAR(20) DEFAULT 'low' COMMENT '风险等级：high, medium, low',
    sensitive_words TEXT COMMENT '敏感词详情',
    detection_time DATETIME COMMENT '检测时间',
    check_status VARCHAR(20) DEFAULT 'pending' COMMENT '检查状态：pending, completed, error',
    error_message TEXT COMMENT '错误信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    CONSTRAINT fk_detail_task FOREIGN KEY (task_id) REFERENCES booklist_check_task(task_id) ON DELETE CASCADE,
    CHECK (hit_sensitive IN (0, 1)),
    CHECK (hit_problem_book IN (0, 1)),
    CHECK (is_whitelist_publisher IN (0, 1)),
    CHECK (risk_level IN ('high', 'medium', 'low')),
    CHECK (check_status IN ('pending', 'completed', 'error'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书目检测详情表 - 适配新模板（2025-10-30）';

CREATE INDEX idx_detail_task_id ON booklist_check_detail(task_id);
CREATE INDEX idx_detail_isbn ON booklist_check_detail(isbn);
CREATE INDEX idx_detail_book_name ON booklist_check_detail(book_name(255));
CREATE INDEX idx_detail_publisher ON booklist_check_detail(publisher);
CREATE INDEX idx_detail_risk_level ON booklist_check_detail(risk_level);
CREATE INDEX idx_detail_check_status ON booklist_check_detail(check_status);

-- =====================================================
-- Database Schema Creation Complete
-- =====================================================
-- Notes:
-- 1. All 9 tables have been created for MySQL
-- 2. All necessary indexes have been added
-- 3. FULLTEXT indexes with ngram parser are used for Chinese text search
-- 4. BOOLEAN types are replaced with TINYINT(1)
-- 5. BIGSERIAL is replaced with BIGINT AUTO_INCREMENT
-- 6. TIMESTAMP is replaced with DATETIME
-- 7. Please execute mysql_data_cn.sql to initialize base data
-- =====================================================

