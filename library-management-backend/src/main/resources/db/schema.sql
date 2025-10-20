-- =====================================================
-- 图书馆问题图书管理系统 - 数据库建表脚本
-- =====================================================
-- 数据库类型: PostgreSQL 15.x
-- 字符集: UTF-8
-- 创建日期: 2025-10-18
-- 说明: 本脚本用于从零创建所有数据库表
-- =====================================================

-- =====================================================
-- 0. 启用必要的 PostgreSQL 扩展
-- =====================================================
-- pg_trgm: 用于模糊匹配加速
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- btree_gin: 用于组合索引优化
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- =====================================================
-- 1. 用户信息表 (sys_user)
-- =====================================================
DROP TABLE IF EXISTS sys_user CASCADE;

CREATE TABLE sys_user (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'user')),
    department VARCHAR(100),
    real_name VARCHAR(50) NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    is_active SMALLINT DEFAULT 1 CHECK (is_active IN (0, 1)),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP
);

-- 用户表索引
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_employee_id ON sys_user(employee_id);
CREATE INDEX idx_user_role ON sys_user(role);

-- 用户表注释
COMMENT ON TABLE sys_user IS '用户信息表';
COMMENT ON COLUMN sys_user.user_id IS '用户ID（主键）';
COMMENT ON COLUMN sys_user.username IS '用户名（唯一）';
COMMENT ON COLUMN sys_user.password_hash IS '密码哈希（BCrypt加密）';
COMMENT ON COLUMN sys_user.role IS '用户角色：admin-管理员, user-普通用户';
COMMENT ON COLUMN sys_user.department IS '所属部门';
COMMENT ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN sys_user.employee_id IS '工号（唯一）';
COMMENT ON COLUMN sys_user.is_active IS '账号状态：1-启用, 0-禁用';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.last_login_time IS '最后登录时间';

-- =====================================================
-- 2. 敏感词分类表 (sensitive_categories)
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

-- 敏感词分类表索引
CREATE INDEX idx_sc_category_name ON sensitive_categories(category_name);

-- 敏感词分类表注释
COMMENT ON TABLE sensitive_categories IS '敏感词分类表';
COMMENT ON COLUMN sensitive_categories.category_id IS '分类ID（主键）';
COMMENT ON COLUMN sensitive_categories.category_name IS '分类名称：作者、书名、出版社、关键词等';
COMMENT ON COLUMN sensitive_categories.description IS '分类描述';
COMMENT ON COLUMN sensitive_categories.created_by IS '创建人ID';
COMMENT ON COLUMN sensitive_categories.create_time IS '创建时间';
COMMENT ON COLUMN sensitive_categories.updated_by IS '最后修改人ID';
COMMENT ON COLUMN sensitive_categories.update_time IS '最后修改时间';

-- =====================================================
-- 3. 敏感词库表 (sensitive_words)
-- =====================================================
DROP TABLE IF EXISTS sensitive_words CASCADE;

CREATE TABLE sensitive_words (
    word_id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    keyword VARCHAR(200) NOT NULL,
    match_type SMALLINT NOT NULL DEFAULT 1 CHECK (match_type IN (0, 1, 2)),
    risk_level SMALLINT NOT NULL DEFAULT 2 CHECK (risk_level IN (1, 2, 3)),
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT fk_sw_category FOREIGN KEY (category_id) REFERENCES sensitive_categories(category_id),
    CONSTRAINT fk_sw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_sw_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

-- 敏感词库表索引（使用 GIN 索引加速模糊查询）
CREATE INDEX idx_sw_keyword ON sensitive_words USING GIN (keyword gin_trgm_ops);
CREATE INDEX idx_sw_category_id ON sensitive_words(category_id);
CREATE INDEX idx_sw_is_active ON sensitive_words(is_active);

-- 敏感词库表注释
COMMENT ON TABLE sensitive_words IS '敏感词库表';
COMMENT ON COLUMN sensitive_words.word_id IS '敏感词ID（主键）';
COMMENT ON COLUMN sensitive_words.category_id IS '所属分类ID';
COMMENT ON COLUMN sensitive_words.keyword IS '敏感词，支持模糊匹配';
COMMENT ON COLUMN sensitive_words.match_type IS '匹配类型：0-精确匹配, 1-模糊匹配, 2-正则表达式';
COMMENT ON COLUMN sensitive_words.risk_level IS '风险等级：1-低, 2-中, 3-高';
COMMENT ON COLUMN sensitive_words.is_active IS '是否启用';
COMMENT ON COLUMN sensitive_words.created_by IS '创建人ID';
COMMENT ON COLUMN sensitive_words.create_time IS '创建时间';
COMMENT ON COLUMN sensitive_words.updated_by IS '最后修改人ID';
COMMENT ON COLUMN sensitive_words.update_time IS '最后修改时间';

-- =====================================================
-- 4. 问题书目库表 (problem_books)
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
    source VARCHAR(500),
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT fk_pb_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_pb_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

-- 问题书目库表索引
CREATE INDEX idx_pb_isbn ON problem_books(isbn);
CREATE INDEX idx_pb_book_name ON problem_books USING GIN (book_name gin_trgm_ops);
CREATE INDEX idx_pb_author ON problem_books(author);
CREATE INDEX idx_pb_publisher ON problem_books(publisher);

-- 问题书目库表注释
COMMENT ON TABLE problem_books IS '问题书目库表';
COMMENT ON COLUMN problem_books.book_id IS '书目ID（主键）';
COMMENT ON COLUMN problem_books.book_name IS '书名';
COMMENT ON COLUMN problem_books.author IS '作者';
COMMENT ON COLUMN problem_books.isbn IS '国际标准书号（支持ISBN-10和ISBN-13）';
COMMENT ON COLUMN problem_books.publisher IS '出版社';
COMMENT ON COLUMN problem_books.publish_year IS '出版年份';
COMMENT ON COLUMN problem_books.problem_type IS '问题类型';
COMMENT ON COLUMN problem_books.source IS '来源说明';
COMMENT ON COLUMN problem_books.created_by IS '创建人ID';
COMMENT ON COLUMN problem_books.create_time IS '创建时间';
COMMENT ON COLUMN problem_books.updated_by IS '最后修改人ID';
COMMENT ON COLUMN problem_books.update_time IS '最后修改时间';

-- =====================================================
-- 5. 出版社白名单表 (publisher_whitelist)
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

-- 出版社白名单表索引
CREATE INDEX idx_pw_name ON publisher_whitelist(publisher_name);
CREATE INDEX idx_pw_is_active ON publisher_whitelist(is_active);

-- 出版社白名单表注释
COMMENT ON TABLE publisher_whitelist IS '出版社白名单表';
COMMENT ON COLUMN publisher_whitelist.publisher_id IS '出版社ID（主键）';
COMMENT ON COLUMN publisher_whitelist.publisher_name IS '出版社名称（唯一）';
COMMENT ON COLUMN publisher_whitelist.years IS '年份批次，用于区分不同年度百大出版社（预留字段）';
COMMENT ON COLUMN publisher_whitelist.is_active IS '是否启用';
COMMENT ON COLUMN publisher_whitelist.created_by IS '创建人ID';
COMMENT ON COLUMN publisher_whitelist.create_time IS '创建时间';

-- =====================================================
-- 6. 已购问题图书库表 (purchased_problem_books)
-- =====================================================
DROP TABLE IF EXISTS purchased_problem_books CASCADE;

CREATE TABLE purchased_problem_books (
    asset_code VARCHAR(50) PRIMARY KEY,
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

-- 已购问题图书表索引
CREATE INDEX idx_ppb_isbn ON purchased_problem_books(isbn);
CREATE INDEX idx_ppb_status ON purchased_problem_books(status);
CREATE INDEX idx_ppb_system_id ON purchased_problem_books(system_id);

-- 已购问题图书表注释
COMMENT ON TABLE purchased_problem_books IS '已购问题图书库表';
COMMENT ON COLUMN purchased_problem_books.asset_code IS '图书资产号，唯一标识（主键）';
COMMENT ON COLUMN purchased_problem_books.system_id IS '来源系统：system_1-Aleph系统, system_2-超星系统';
COMMENT ON COLUMN purchased_problem_books.isbn IS '国际标准书号';
COMMENT ON COLUMN purchased_problem_books.book_name IS '书名';
COMMENT ON COLUMN purchased_problem_books.author IS '作者';
COMMENT ON COLUMN purchased_problem_books.publisher IS '出版社';
COMMENT ON COLUMN purchased_problem_books.purchase_date IS '采购日期';
COMMENT ON COLUMN purchased_problem_books.location IS '馆藏位置';
COMMENT ON COLUMN purchased_problem_books.problem_reason IS '问题原因';
COMMENT ON COLUMN purchased_problem_books.status IS '处理状态：pending-待处理, processing-处理中, processed-已处理, reinstated-已恢复';
COMMENT ON COLUMN purchased_problem_books.handler_id IS '处理人ID';
COMMENT ON COLUMN purchased_problem_books.handler_date IS '处理时间';

-- =====================================================
-- 7. 操作日志表 (operation_log)
-- =====================================================
DROP TABLE IF EXISTS operation_log CASCADE;

CREATE TABLE operation_log (
    log_id BIGSERIAL PRIMARY KEY,
    module VARCHAR(100) NOT NULL,
    operation_type VARCHAR(20) NOT NULL CHECK (operation_type IN ('create', 'update', 'delete')),
    target_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    operated_by BIGINT NOT NULL,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    CONSTRAINT fk_log_operator FOREIGN KEY (operated_by) REFERENCES sys_user(user_id)
);

-- 操作日志表索引
CREATE INDEX idx_log_module ON operation_log(module);
CREATE INDEX idx_log_operation_type ON operation_log(operation_type);
CREATE INDEX idx_log_operated_by ON operation_log(operated_by);
CREATE INDEX idx_log_operation_time ON operation_log(operation_time);

-- 操作日志表注释
COMMENT ON TABLE operation_log IS '操作日志表';
COMMENT ON COLUMN operation_log.log_id IS '日志ID（主键）';
COMMENT ON COLUMN operation_log.module IS '操作模块，如：sys_user、sensitive_words等';
COMMENT ON COLUMN operation_log.operation_type IS '操作类型：create-新增, update-修改, delete-删除';
COMMENT ON COLUMN operation_log.target_id IS '目标记录ID';
COMMENT ON COLUMN operation_log.old_value IS 'JSON格式存储变更前数据';
COMMENT ON COLUMN operation_log.new_value IS 'JSON格式存储变更后数据';
COMMENT ON COLUMN operation_log.operated_by IS '操作人ID';
COMMENT ON COLUMN operation_log.operation_time IS '操作时间';
COMMENT ON COLUMN operation_log.ip_address IS '操作IP地址';

-- =====================================================
-- 8. 书单检测任务表 (booklist_check_task)
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
    total_books INT DEFAULT 0,
    sensitive_hits INT DEFAULT 0,
    problem_book_hits INT DEFAULT 0,
    non_whitelist_pubs INT DEFAULT 0,
    total_problem_books INT DEFAULT 0,
    error_message TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    CONSTRAINT fk_task_submitter FOREIGN KEY (submitted_by) REFERENCES sys_user(user_id)
);

-- 书单检测任务表索引
CREATE INDEX idx_task_submitted_by ON booklist_check_task(submitted_by);
CREATE INDEX idx_task_submit_time ON booklist_check_task(submit_time);
CREATE INDEX idx_task_status ON booklist_check_task(status);

-- 书单检测任务表注释
COMMENT ON TABLE booklist_check_task IS '书单检测任务表';
COMMENT ON COLUMN booklist_check_task.task_id IS '任务ID（主键）';
COMMENT ON COLUMN booklist_check_task.task_name IS '检测任务名称，格式：提交人_日期_次数';
COMMENT ON COLUMN booklist_check_task.task_type IS '任务类型：批量检测、单书检测';
COMMENT ON COLUMN booklist_check_task.submitted_by IS '提交人ID';
COMMENT ON COLUMN booklist_check_task.submit_time IS '提交时间';
COMMENT ON COLUMN booklist_check_task.start_time IS '开始执行时间';
COMMENT ON COLUMN booklist_check_task.end_time IS '结束时间';
COMMENT ON COLUMN booklist_check_task.original_filename IS '原始文件名';
COMMENT ON COLUMN booklist_check_task.file_path IS '上传文件存储路径';
COMMENT ON COLUMN booklist_check_task.result_file_path IS '结果文件存储路径';
COMMENT ON COLUMN booklist_check_task.status IS '任务状态：pending-待处理, processing-处理中, success-成功, failed-失败, cancelled-已取消';
COMMENT ON COLUMN booklist_check_task.total_books IS '总书目数量';
COMMENT ON COLUMN booklist_check_task.sensitive_hits IS '命中敏感词数量';
COMMENT ON COLUMN booklist_check_task.problem_book_hits IS '命中问题书目数量';
COMMENT ON COLUMN booklist_check_task.non_whitelist_pubs IS '非白名单出版社数量';
COMMENT ON COLUMN booklist_check_task.total_problem_books IS '问题图书总数';
COMMENT ON COLUMN booklist_check_task.error_message IS '错误信息';
COMMENT ON COLUMN booklist_check_task.created_time IS '创建时间';
COMMENT ON COLUMN booklist_check_task.update_time IS '更新时间';

-- =====================================================
-- 9. 书单检测结果明细表 (booklist_check_detail)
-- =====================================================
DROP TABLE IF EXISTS booklist_check_detail CASCADE;

CREATE TABLE booklist_check_detail (
    detail_id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    isbn VARCHAR(20),
    book_name VARCHAR(500),
    author VARCHAR(200),
    publisher VARCHAR(200),
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

-- 书单检测明细表索引
CREATE INDEX idx_detail_task_id ON booklist_check_detail(task_id);
CREATE INDEX idx_detail_risk_level ON booklist_check_detail(risk_level);
CREATE INDEX idx_detail_check_status ON booklist_check_detail(check_status);

-- 书单检测明细表注释
COMMENT ON TABLE booklist_check_detail IS '书单检测结果明细表';
COMMENT ON COLUMN booklist_check_detail.detail_id IS '明细ID（主键）';
COMMENT ON COLUMN booklist_check_detail.task_id IS '所属任务ID';
COMMENT ON COLUMN booklist_check_detail.isbn IS '国际标准书号';
COMMENT ON COLUMN booklist_check_detail.book_name IS '书名';
COMMENT ON COLUMN booklist_check_detail.author IS '作者';
COMMENT ON COLUMN booklist_check_detail.publisher IS '出版社';
COMMENT ON COLUMN booklist_check_detail.hit_sensitive IS '是否命中敏感词：0-否, 1-是';
COMMENT ON COLUMN booklist_check_detail.hit_problem_book IS '是否命中问题书目：0-否, 1-是';
COMMENT ON COLUMN booklist_check_detail.is_whitelist_publisher IS '出版社是否在白名单：0-否, 1-是';
COMMENT ON COLUMN booklist_check_detail.risk_level IS '风险等级：high-高, medium-中, low-低';
COMMENT ON COLUMN booklist_check_detail.sensitive_words IS '命中的敏感词详情，存储具体的敏感词列表';
COMMENT ON COLUMN booklist_check_detail.detection_time IS '检测时间';
COMMENT ON COLUMN booklist_check_detail.check_status IS '检测状态：pending-待检测, completed-已完成, error-检测失败';
COMMENT ON COLUMN booklist_check_detail.error_message IS '错误信息';
COMMENT ON COLUMN booklist_check_detail.created_time IS '创建时间';
COMMENT ON COLUMN booklist_check_detail.updated_time IS '更新时间';

-- =====================================================
-- 建表完成提示
-- =====================================================
-- 说明：
-- 1. 本脚本已创建所有 9 张数据表
-- 2. 所有表已添加必要的索引以优化查询性能
-- 3. 所有表已添加完整的字段注释
-- 4. 敏感词和书名字段使用 GIN 索引加速模糊查询
-- 5. 请执行 data.sql 脚本初始化基础数据
-- =====================================================
