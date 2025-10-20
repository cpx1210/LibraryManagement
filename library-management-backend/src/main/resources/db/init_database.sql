-- 图书馆问题图书管理系统 - 数据库建表脚本
-- 数据库: PostgreSQL 15.x

-- 启用必要的扩展
CREATE EXTENSION IF NOT EXISTS pg_trgm;
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
                          last_login_time TIMESTAMP,
                          created_by BIGINT NULL ,
                          CONSTRAINT fk_user_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_employee_id ON sys_user(employee_id);
CREATE INDEX idx_user_role ON sys_user(role);

COMMENT ON TABLE sys_user IS '用户信息表';
COMMENT ON COLUMN sys_user.employee_id IS '工号，用于生成用户名';
COMMENT ON COLUMN sys_user.role IS '用户角色：admin-管理员, user-普通用户';

-- =====================================================
-- 2. 敏感词库表 (sensitive_words)
-- =====================================================
DROP TABLE IF EXISTS sensitive_words CASCADE;

CREATE TABLE sensitive_words (
                                 word_id BIGSERIAL PRIMARY KEY,
                                 keyword VARCHAR(200) NOT NULL,
                                 category VARCHAR(100),
                                 created_by BIGINT NOT NULL,
                                 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_by BIGINT,
                                 update_time TIMESTAMP,
                                 CONSTRAINT fk_sw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
                                 CONSTRAINT fk_sw_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_sw_keyword ON sensitive_words USING GIN (keyword gin_trgm_ops);
CREATE INDEX idx_sw_category ON sensitive_words(category);

COMMENT ON TABLE sensitive_words IS '敏感词库表';
COMMENT ON COLUMN sensitive_words.keyword IS '敏感词，支持模糊匹配';

-- =====================================================
-- 3. 问题书目库表 (problem_books)
-- =====================================================
DROP TABLE IF EXISTS problem_books CASCADE;

CREATE TABLE problem_books (
                               book_id BIGSERIAL PRIMARY KEY,
                               isbn VARCHAR(20),
                               book_name VARCHAR(500) NOT NULL,
                               author VARCHAR(200),
                               publisher VARCHAR(200),
                               problem_type VARCHAR(100),
                               source VARCHAR(500),
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

COMMENT ON TABLE problem_books IS '问题书目库表';
COMMENT ON COLUMN problem_books.isbn IS '支持ISBN-10和ISBN-13';

-- =====================================================
-- 4. 出版社白名单表 (publisher_whitelist)
-- =====================================================
DROP TABLE IF EXISTS publisher_whitelist CASCADE;

CREATE TABLE publisher_whitelist (
                                     publisher_id BIGSERIAL PRIMARY KEY,
                                     publisher_name VARCHAR(200) UNIQUE NOT NULL,
                                     years BIGINT,
                                     status VARCHAR(20) DEFAULT 'valid' CHECK (status IN ('valid', 'invalid')),
                                     created_by BIGINT NOT NULL,
                                     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_pw_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_pw_name ON publisher_whitelist(publisher_name);
CREATE INDEX idx_pw_status ON publisher_whitelist(status);

COMMENT ON TABLE publisher_whitelist IS '出版社白名单表';
COMMENT ON COLUMN publisher_whitelist.years IS '年份批次字段，业务逻辑待明确';
COMMENT ON COLUMN publisher_whitelist.status IS '状态字段，业务逻辑待明确';

-- =====================================================
-- 5. 已购问题图书库表 (purchased_problem_books)
-- =====================================================
DROP TABLE IF EXISTS purchased_problem_books CASCADE;

CREATE TABLE purchased_problem_books (
                                         id BIGSERIAL PRIMARY KEY,
                                         asset_code VARCHAR(50) UNIQUE NOT NULL,
                                         system_id VARCHAR(20) CHECK (system_id IN ('system_1', 'system_2')),
                                         isbn VARCHAR(20),
                                         book_name VARCHAR(500) NOT NULL,
                                         author VARCHAR(200),
                                         publisher VARCHAR(200),
                                         purchase_date DATE,
                                         location VARCHAR(200),
                                         problem_reason TEXT,
                                         status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'processed', 'reinstated')),
                                         handler_id BIGINT,
                                         handler_date TIMESTAMP,
                                         created_by BIGINT NOT NULL,
                                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_by BIGINT,
                                         update_time TIMESTAMP,
                                         CONSTRAINT fk_ppb_handler FOREIGN KEY (handler_id) REFERENCES sys_user(user_id),
                                         CONSTRAINT fk_ppb_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
                                         CONSTRAINT fk_ppb_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_ppb_asset_code ON purchased_problem_books(asset_code);
CREATE INDEX idx_ppb_isbn ON purchased_problem_books(isbn);
CREATE INDEX idx_ppb_status ON purchased_problem_books(status);
CREATE INDEX idx_ppb_system_id ON purchased_problem_books(system_id);

COMMENT ON TABLE purchased_problem_books IS '已购问题图书库表';
COMMENT ON COLUMN purchased_problem_books.system_id IS 'system_1: Aleph系统, system_2: 超星系统';
COMMENT ON COLUMN purchased_problem_books.asset_code IS '图书资产号';

-- =====================================================
-- 6. 操作日志表 (operation_log)
-- =====================================================
DROP TABLE IF EXISTS operation_log CASCADE;

CREATE TABLE operation_log (
                               log_id BIGSERIAL PRIMARY KEY,
                               module VARCHAR(100) NOT NULL,
                               operation_type VARCHAR(20) NOT NULL CHECK (operation_type IN ('create', 'update', 'delete', 'query', 'export', 'import')),
                               target_id BIGINT,
                               target_type VARCHAR(50),
                               old_value TEXT,
                               new_value TEXT,
                               operated_by BIGINT NOT NULL,
                               operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               ip_address VARCHAR(45),
                               user_agent VARCHAR(500),
                               remark VARCHAR(500),
                               CONSTRAINT fk_log_operator FOREIGN KEY (operated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_log_module ON operation_log(module);
CREATE INDEX idx_log_operation_type ON operation_log(operation_type);
CREATE INDEX idx_log_operated_by ON operation_log(operated_by);
CREATE INDEX idx_log_operation_time ON operation_log(operation_time);
CREATE INDEX idx_log_target ON operation_log(target_type, target_id);

COMMENT ON TABLE operation_log IS '操作日志表';
COMMENT ON COLUMN operation_log.old_value IS 'JSON格式存储变更前数据';
COMMENT ON COLUMN operation_log.new_value IS 'JSON格式存储变更后数据';

-- =====================================================
-- 7. 书单检测任务表 (booklist_check_task)
-- =====================================================
DROP TABLE IF EXISTS booklist_check_task CASCADE;

CREATE TABLE booklist_check_task (
                                     task_id BIGSERIAL PRIMARY KEY,
                                     task_name VARCHAR(255) NOT NULL,
                                     submitted_by BIGINT NOT NULL,
                                     submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     original_filename VARCHAR(255),
                                     original_file_path VARCHAR(500),
                                     result_file_path VARCHAR(500),
                                     status VARCHAR(20) DEFAULT 'uploading' CHECK (status IN ('uploading', 'processing', 'success', 'failed')),
                                     total_books INT DEFAULT 0,
                                     sensitive_hits INT DEFAULT 0,
                                     problem_book_hits INT DEFAULT 0,
                                     non_whitelist_pubs INT DEFAULT 0,
                                     total_problem_books INT DEFAULT 0,
                                     error_message TEXT,
                                     expire_date DATE,
                                     CONSTRAINT fk_task_submitter FOREIGN KEY (submitted_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_task_submitted_by ON booklist_check_task(submitted_by);
CREATE INDEX idx_task_submit_time ON booklist_check_task(submit_time);
CREATE INDEX idx_task_status ON booklist_check_task(status);
CREATE INDEX idx_task_expire_date ON booklist_check_task(expire_date);

COMMENT ON TABLE booklist_check_task IS '书单检测任务表';
COMMENT ON COLUMN booklist_check_task.expire_date IS '检测记录保留1年，过期后自动清理';

-- =====================================================
-- 8. 书单检测结果明细表 (booklist_check_detail)
-- =====================================================
DROP TABLE IF EXISTS booklist_check_detail CASCADE;

CREATE TABLE booklist_check_detail (
                                       detail_id BIGSERIAL PRIMARY KEY,
                                       task_id BIGINT NOT NULL,
                                       row_number INT NOT NULL,
                                       isbn VARCHAR(20),
                                       book_name VARCHAR(500),
                                       author VARCHAR(200),
                                       publisher VARCHAR(200),
                                       hit_sensitive SMALLINT DEFAULT 0 CHECK (hit_sensitive IN (0, 1)),
                                       hit_problem_book SMALLINT DEFAULT 0 CHECK (hit_problem_book IN (0, 1)),
                                       is_whitelist_publisher SMALLINT DEFAULT 1 CHECK (is_whitelist_publisher IN (0, 1)),
                                       risk_level VARCHAR(20) DEFAULT 'low' CHECK (risk_level IN ('high', 'medium', 'low')),
                                       matched_keywords TEXT,
                                       remark TEXT,
                                       CONSTRAINT fk_detail_task FOREIGN KEY (task_id) REFERENCES booklist_check_task(task_id) ON DELETE CASCADE
);

CREATE INDEX idx_detail_task_id ON booklist_check_detail(task_id);
CREATE INDEX idx_detail_risk_level ON booklist_check_detail(risk_level);

COMMENT ON TABLE booklist_check_detail IS '书单检测结果明细表';
COMMENT ON COLUMN booklist_check_detail.matched_keywords IS '命中的具体敏感词，用于展示';
COMMENT ON COLUMN booklist_check_detail.remark IS '综合备注信息';
