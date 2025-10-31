-- =====================================================
-- Library Problem Books Management System - Database Schema
-- =====================================================
-- Database: PostgreSQL 15.x
-- Encoding: UTF-8
-- Updated: 2025-10-30
-- Description: Creates all database tables from scratch
-- =====================================================

-- Set client encoding to UTF8
SET client_encoding = 'UTF8';

-- =====================================================
-- 0. Enable PostgreSQL Extensions
-- =====================================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- =====================================================
-- 1. sys_user
-- =====================================================
DROP TABLE IF EXISTS sys_user CASCADE;

CREATE TABLE sys_user (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'user')),
    department VARCHAR(100),
    real_name VARCHAR(50) NOT NULL,
    is_active SMALLINT DEFAULT 1 CHECK (is_active IN (0, 1)),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP
);

CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_employee_id ON sys_user(employee_id);
CREATE INDEX idx_user_role ON sys_user(role);

COMMENT ON TABLE sys_user IS 'User Information Table';
COMMENT ON COLUMN sys_user.user_id IS 'User ID (Primary Key)';
COMMENT ON COLUMN sys_user.username IS 'Username (Unique, for login)';
COMMENT ON COLUMN sys_user.password_hash IS 'Password Hash (BCrypt)';
COMMENT ON COLUMN sys_user.employee_id IS 'Employee ID (Unique)';
COMMENT ON COLUMN sys_user.role IS 'User Role: admin, user';
COMMENT ON COLUMN sys_user.department IS 'Department';
COMMENT ON COLUMN sys_user.real_name IS 'Real Name';
COMMENT ON COLUMN sys_user.is_active IS 'Account Status: 1-Active, 0-Disabled';
COMMENT ON COLUMN sys_user.create_time IS 'Create Time';
COMMENT ON COLUMN sys_user.last_login_time IS 'Last Login Time';

-- =====================================================
-- 2. sensitive_categories
-- =====================================================
DROP TABLE IF EXISTS sensitive_categories CASCADE;

CREATE TABLE sensitive_categories (
    category_id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT fk_sc_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_sc_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_sc_category_name ON sensitive_categories(category_name);

COMMENT ON TABLE sensitive_categories IS 'Sensitive Word Categories Table';
COMMENT ON COLUMN sensitive_categories.category_id IS 'Category ID (Primary Key)';
COMMENT ON COLUMN sensitive_categories.category_name IS 'Category Name: Author, Book Title, Publisher, Keyword';
COMMENT ON COLUMN sensitive_categories.description IS 'Category Description';
COMMENT ON COLUMN sensitive_categories.created_by IS 'Creator User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN sensitive_categories.create_time IS 'Create Time';
COMMENT ON COLUMN sensitive_categories.updated_by IS 'Updater User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN sensitive_categories.update_time IS 'Update Time';

-- =====================================================
-- 3. sensitive_words
-- =====================================================
DROP TABLE IF EXISTS sensitive_words CASCADE;

CREATE TABLE sensitive_words (
    word_id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    match_type SMALLINT NOT NULL DEFAULT 1 CHECK (match_type IN (0, 1, 2)),
    risk_level SMALLINT NOT NULL DEFAULT 2 CHECK (risk_level IN (1, 2, 3)),
    detection_type VARCHAR(20) NOT NULL DEFAULT '关键词' CHECK (detection_type IN ('关键词', '书名', '作者')),
    alert_message VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT fk_sw_category FOREIGN KEY (category_id) REFERENCES sensitive_categories(category_id),
    CONSTRAINT fk_sw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_sw_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_sw_keyword ON sensitive_words USING GIN (keyword gin_trgm_ops);
CREATE INDEX idx_sw_category_id ON sensitive_words(category_id);
CREATE INDEX idx_sw_detection_type ON sensitive_words(detection_type);
CREATE INDEX idx_sw_is_active ON sensitive_words(is_active);

COMMENT ON TABLE sensitive_words IS 'Sensitive Words Table';
COMMENT ON COLUMN sensitive_words.word_id IS 'Word ID (Primary Key)';
COMMENT ON COLUMN sensitive_words.category_id IS 'Category ID (FK -> sensitive_categories.category_id)';
COMMENT ON COLUMN sensitive_words.keyword IS 'Sensitive Keyword (max 100 chars)';
COMMENT ON COLUMN sensitive_words.match_type IS 'Match Type: 0-Exact, 1-Fuzzy, 2-Regex';
COMMENT ON COLUMN sensitive_words.risk_level IS 'Risk Level: 1-Low, 2-Medium, 3-High';
COMMENT ON COLUMN sensitive_words.detection_type IS 'Detection Type: 关键词-Global, 书名-BookName Only, 作者-Author Only';
COMMENT ON COLUMN sensitive_words.alert_message IS 'Alert Message: Warning message when keyword is matched';
COMMENT ON COLUMN sensitive_words.is_active IS 'Is Active (default true)';
COMMENT ON COLUMN sensitive_words.created_by IS 'Creator User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN sensitive_words.create_time IS 'Create Time';
COMMENT ON COLUMN sensitive_words.updated_by IS 'Updater User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN sensitive_words.update_time IS 'Update Time';

-- =====================================================
-- 4. problem_books
-- =====================================================
DROP TABLE IF EXISTS problem_books CASCADE;

CREATE TABLE problem_books (
    book_id BIGSERIAL PRIMARY KEY,
    book_name VARCHAR(500) NOT NULL,
    author VARCHAR(200),
    isbn VARCHAR(20),
    publisher VARCHAR(200),
    publish_year VARCHAR(4),
    problem_type VARCHAR(100),
    source VARCHAR(255),
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT fk_pb_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_pb_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_pb_isbn ON problem_books(isbn);
CREATE INDEX idx_pb_book_name ON problem_books USING GIN (book_name gin_trgm_ops);
CREATE INDEX idx_pb_author ON problem_books(author);
CREATE INDEX idx_pb_publisher ON problem_books(publisher);

COMMENT ON TABLE problem_books IS 'Problem Books Table';
COMMENT ON COLUMN problem_books.book_id IS 'Book ID (Primary Key)';
COMMENT ON COLUMN problem_books.book_name IS 'Book Name';
COMMENT ON COLUMN problem_books.author IS 'Author';
COMMENT ON COLUMN problem_books.isbn IS 'ISBN';
COMMENT ON COLUMN problem_books.publisher IS 'Publisher';
COMMENT ON COLUMN problem_books.publish_year IS 'Publish Year (Format: YYYY)';
COMMENT ON COLUMN problem_books.problem_type IS 'Problem Type';
COMMENT ON COLUMN problem_books.source IS 'Source';
COMMENT ON COLUMN problem_books.created_by IS 'Creator User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN problem_books.create_time IS 'Create Time';
COMMENT ON COLUMN problem_books.updated_by IS 'Updater User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN problem_books.update_time IS 'Update Time';

-- =====================================================
-- 5. publisher_whitelist
-- =====================================================
DROP TABLE IF EXISTS publisher_whitelist CASCADE;

CREATE TABLE publisher_whitelist (
    publisher_id BIGSERIAL PRIMARY KEY,
    publisher_name VARCHAR(200) UNIQUE NOT NULL,
    years BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_pw_name ON publisher_whitelist(publisher_name);
CREATE INDEX idx_pw_is_active ON publisher_whitelist(is_active);

COMMENT ON TABLE publisher_whitelist IS 'Publisher Whitelist Table';
COMMENT ON COLUMN publisher_whitelist.publisher_id IS 'Publisher ID (Primary Key)';
COMMENT ON COLUMN publisher_whitelist.publisher_name IS 'Publisher Name (Unique)';
COMMENT ON COLUMN publisher_whitelist.years IS 'Year Batch';
COMMENT ON COLUMN publisher_whitelist.is_active IS 'Is Active (default true)';
COMMENT ON COLUMN publisher_whitelist.created_by IS 'Creator User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN publisher_whitelist.create_time IS 'Create Time';

-- =====================================================
-- 6. purchased_problem_books
-- =====================================================
DROP TABLE IF EXISTS purchased_problem_books CASCADE;

CREATE TABLE purchased_problem_books (
    asset_code BIGSERIAL PRIMARY KEY,
    system_id VARCHAR(20) CHECK (system_id IN ('system_1', 'system_2')),
    isbn VARCHAR(20),
    book_name VARCHAR(500) NOT NULL,
    author VARCHAR(200),
    publisher VARCHAR(200),
    purchase_date DATE,
    location VARCHAR(100),
    problem_reason TEXT,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'processed', 'reinstated')),
    handler_id BIGINT,
    handler_date TIMESTAMP,
    CONSTRAINT fk_ppb_handler FOREIGN KEY (handler_id) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_ppb_isbn ON purchased_problem_books(isbn);
CREATE INDEX idx_ppb_status ON purchased_problem_books(status);
CREATE INDEX idx_ppb_system_id ON purchased_problem_books(system_id);

COMMENT ON TABLE purchased_problem_books IS 'Purchased Problem Books Table';
COMMENT ON COLUMN purchased_problem_books.asset_code IS 'Asset Code (Primary Key, Auto-increment)';
COMMENT ON COLUMN purchased_problem_books.system_id IS 'System ID: system_1-Aleph, system_2-Chaoxing';
COMMENT ON COLUMN purchased_problem_books.isbn IS 'ISBN';
COMMENT ON COLUMN purchased_problem_books.book_name IS 'Book Name';
COMMENT ON COLUMN purchased_problem_books.author IS 'Author';
COMMENT ON COLUMN purchased_problem_books.publisher IS 'Publisher';
COMMENT ON COLUMN purchased_problem_books.purchase_date IS 'Purchase Date';
COMMENT ON COLUMN purchased_problem_books.location IS 'Storage Location';
COMMENT ON COLUMN purchased_problem_books.problem_reason IS 'Problem Reason';
COMMENT ON COLUMN purchased_problem_books.status IS 'Status: pending, processing, processed, reinstated';
COMMENT ON COLUMN purchased_problem_books.handler_id IS 'Handler User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN purchased_problem_books.handler_date IS 'Handler Date';

-- =====================================================
-- 7. operation_log
-- =====================================================
DROP TABLE IF EXISTS operation_log CASCADE;

CREATE TABLE operation_log (
    log_id BIGSERIAL PRIMARY KEY,
    module VARCHAR(50) NOT NULL,
    operation_type VARCHAR(20) NOT NULL CHECK (operation_type IN ('create', 'update', 'delete')),
    target_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    operated_by BIGINT NOT NULL,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    CONSTRAINT fk_log_operator FOREIGN KEY (operated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_log_module ON operation_log(module);
CREATE INDEX idx_log_operation_type ON operation_log(operation_type);
CREATE INDEX idx_log_operated_by ON operation_log(operated_by);
CREATE INDEX idx_log_operation_time ON operation_log(operation_time);

COMMENT ON TABLE operation_log IS 'Operation Log Table';
COMMENT ON COLUMN operation_log.log_id IS 'Log ID (Primary Key)';
COMMENT ON COLUMN operation_log.module IS 'Module (e.g., purchased_problem_books)';
COMMENT ON COLUMN operation_log.operation_type IS 'Operation Type: create, update, delete';
COMMENT ON COLUMN operation_log.target_id IS 'Target Record ID';
COMMENT ON COLUMN operation_log.old_value IS 'Old Value (JSON format)';
COMMENT ON COLUMN operation_log.new_value IS 'New Value (JSON format)';
COMMENT ON COLUMN operation_log.operated_by IS 'Operator User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN operation_log.operation_time IS 'Operation Time';
COMMENT ON COLUMN operation_log.ip_address IS 'IP Address';

-- =====================================================
-- 8. booklist_check_task
-- =====================================================
DROP TABLE IF EXISTS booklist_check_task CASCADE;

CREATE TABLE booklist_check_task (
    task_id BIGSERIAL PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    task_type VARCHAR(50),
    submitted_by BIGINT NOT NULL,
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    original_filename VARCHAR(500),
    file_path VARCHAR(500),
    result_file_path VARCHAR(500),
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'success', 'failed', 'cancelled')),
    error_message TEXT,
    sensitive_hits INT DEFAULT 0,
    problem_book_hits INT DEFAULT 0,
    non_whitelist_pubs INT DEFAULT 0,
    total_books INT DEFAULT 0,
    total_problem_books INT DEFAULT 0,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    CONSTRAINT fk_task_submitter FOREIGN KEY (submitted_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_task_submitted_by ON booklist_check_task(submitted_by);
CREATE INDEX idx_task_submit_time ON booklist_check_task(submit_time);
CREATE INDEX idx_task_status ON booklist_check_task(status);

COMMENT ON TABLE booklist_check_task IS 'Booklist Check Task Table';
COMMENT ON COLUMN booklist_check_task.task_id IS 'Task ID (Primary Key)';
COMMENT ON COLUMN booklist_check_task.task_name IS 'Task Name (Format: Submitter_Date_Sequence)';
COMMENT ON COLUMN booklist_check_task.task_type IS 'Task Type: Batch Check, Single Book Check';
COMMENT ON COLUMN booklist_check_task.submitted_by IS 'Submitter User ID (FK -> sys_user.user_id)';
COMMENT ON COLUMN booklist_check_task.submit_time IS 'Submit Time';
COMMENT ON COLUMN booklist_check_task.start_time IS 'Start Time';
COMMENT ON COLUMN booklist_check_task.end_time IS 'End Time';
COMMENT ON COLUMN booklist_check_task.original_filename IS 'Original Filename';
COMMENT ON COLUMN booklist_check_task.file_path IS 'File Storage Path';
COMMENT ON COLUMN booklist_check_task.result_file_path IS 'Result File Path';
COMMENT ON COLUMN booklist_check_task.status IS 'Status: pending, processing, success, failed, cancelled';
COMMENT ON COLUMN booklist_check_task.error_message IS 'Error Message';
COMMENT ON COLUMN booklist_check_task.sensitive_hits IS 'Sensitive Word Hits Count';
COMMENT ON COLUMN booklist_check_task.problem_book_hits IS 'Problem Book Hits Count';
COMMENT ON COLUMN booklist_check_task.non_whitelist_pubs IS 'Non-Whitelist Publishers Count';
COMMENT ON COLUMN booklist_check_task.total_books IS 'Total Books Count';
COMMENT ON COLUMN booklist_check_task.total_problem_books IS 'Total Problem Books Count';
COMMENT ON COLUMN booklist_check_task.created_time IS 'Create Time';
COMMENT ON COLUMN booklist_check_task.update_time IS 'Update Time';

-- =====================================================
-- 9. booklist_check_detail
-- =====================================================
DROP TABLE IF EXISTS booklist_check_detail CASCADE;

CREATE TABLE booklist_check_detail (
    detail_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    book_number VARCHAR(50),
    book_name VARCHAR(500),
    subtitle VARCHAR(200),
    author1 VARCHAR(100),
    author2 VARCHAR(100),
    author VARCHAR(100),
    isbn VARCHAR(20),
    publish_location VARCHAR(100),
    publisher VARCHAR(100),
    publish_date VARCHAR(50),
    target_audience VARCHAR(100),
    content_summary TEXT,
    classification_number VARCHAR(50),
    language VARCHAR(50),
    hit_sensitive SMALLINT DEFAULT 0 CHECK (hit_sensitive IN (0, 1)),
    hit_problem_book SMALLINT DEFAULT 0 CHECK (hit_problem_book IN (0, 1)),
    is_whitelist_publisher SMALLINT DEFAULT 1 CHECK (is_whitelist_publisher IN (0, 1)),
    risk_level VARCHAR(20) DEFAULT 'low' CHECK (risk_level IN ('high', 'medium', 'low')),
    sensitive_words TEXT,
    detection_time TIMESTAMP,
    check_status VARCHAR(20) DEFAULT 'pending' CHECK (check_status IN ('pending', 'completed', 'error')),
    error_message TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP,
    CONSTRAINT fk_detail_task FOREIGN KEY (task_id) REFERENCES booklist_check_task(task_id) ON DELETE CASCADE
);

CREATE INDEX idx_detail_task_id ON booklist_check_detail(task_id);
CREATE INDEX idx_detail_isbn ON booklist_check_detail(isbn);
CREATE INDEX idx_detail_book_name ON booklist_check_detail(book_name);
CREATE INDEX idx_detail_publisher ON booklist_check_detail(publisher);
CREATE INDEX idx_detail_risk_level ON booklist_check_detail(risk_level);
CREATE INDEX idx_detail_check_status ON booklist_check_detail(check_status);

COMMENT ON TABLE booklist_check_detail IS 'Booklist Check Detail Table - Updated for new template (2025-10-30)';
COMMENT ON COLUMN booklist_check_detail.detail_id IS 'Detail ID (Primary Key)';
COMMENT ON COLUMN booklist_check_detail.task_id IS 'Task ID (FK -> booklist_check_task.task_id)';
COMMENT ON COLUMN booklist_check_detail.book_number IS 'Book Number';
COMMENT ON COLUMN booklist_check_detail.book_name IS 'Book Name (Title)';
COMMENT ON COLUMN booklist_check_detail.subtitle IS 'Subtitle';
COMMENT ON COLUMN booklist_check_detail.author1 IS 'Author 1 (First Author)';
COMMENT ON COLUMN booklist_check_detail.author2 IS 'Author 2 (Second Author)';
COMMENT ON COLUMN booklist_check_detail.author IS 'Combined Author (for compatibility)';
COMMENT ON COLUMN booklist_check_detail.isbn IS 'ISBN';
COMMENT ON COLUMN booklist_check_detail.publish_location IS 'Publish Location';
COMMENT ON COLUMN booklist_check_detail.publisher IS 'Publisher';
COMMENT ON COLUMN booklist_check_detail.publish_date IS 'Publish Date';
COMMENT ON COLUMN booklist_check_detail.target_audience IS 'Target Audience';
COMMENT ON COLUMN booklist_check_detail.content_summary IS 'Content Summary';
COMMENT ON COLUMN booklist_check_detail.classification_number IS 'Classification Number';
COMMENT ON COLUMN booklist_check_detail.language IS 'Language';
COMMENT ON COLUMN booklist_check_detail.hit_sensitive IS 'Hit Sensitive Word: 1-Yes, 0-No';
COMMENT ON COLUMN booklist_check_detail.hit_problem_book IS 'Hit Problem Book: 1-Yes, 0-No';
COMMENT ON COLUMN booklist_check_detail.is_whitelist_publisher IS 'Is Whitelist Publisher: 1-Yes, 0-No';
COMMENT ON COLUMN booklist_check_detail.risk_level IS 'Risk Level: high, medium, low';
COMMENT ON COLUMN booklist_check_detail.sensitive_words IS 'Sensitive Words Details';
COMMENT ON COLUMN booklist_check_detail.detection_time IS 'Detection Time';
COMMENT ON COLUMN booklist_check_detail.check_status IS 'Check Status: pending, completed, error';
COMMENT ON COLUMN booklist_check_detail.error_message IS 'Error Message';
COMMENT ON COLUMN booklist_check_detail.created_time IS 'Create Time';
COMMENT ON COLUMN booklist_check_detail.updated_time IS 'Update Time';

-- =====================================================
-- Database Schema Creation Complete
-- =====================================================
-- Notes:
-- 1. All 9 tables have been created
-- 2. All necessary indexes have been added
-- 3. All field comments have been added
-- 4. GIN indexes are used for fuzzy search on sensitive words and book names
-- 5. asset_code in purchased_problem_books is BIGSERIAL auto-increment
-- 6. Please execute data.sql to initialize base data
-- =====================================================
