-- =====================================================
-- 图书馆问题图书管理系统 - MySQL 数据库建表脚本
-- =====================================================
-- 数据库类型: MySQL 8.0+
-- 字符集: UTF-8 (utf8mb4)
-- 创建日期: 2025-12-08
-- 说明: 从零开始创建所有数据库表结构
-- 执行顺序: 先执行此脚本，再执行 02_mysql_init_data.sql
-- =====================================================

-- =====================================================
-- 第一部分：初始化设置
-- =====================================================

-- 设置字符编码为 UTF-8（支持完整的 Unicode 字符集，包括 emoji）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection = utf8mb4;

-- =====================================================
-- 第二部分：创建数据库
-- =====================================================

-- 如果数据库已存在则删除（⚠️ 生产环境请谨慎使用！）
-- DROP DATABASE IF EXISTS library_management;

-- 创建数据库（如果不存在）
-- 注意：MySQL 8.0 的 CREATE DATABASE 不支持 COMMENT，改用注释说明
CREATE DATABASE IF NOT EXISTS library_management 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 选择数据库
USE library_management;

-- =====================================================
-- 第三部分：删除已存在的表（按依赖关系倒序删除）
-- =====================================================

-- 禁用外键检查（方便删除表）
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有表（如果存在）
DROP TABLE IF EXISTS booklist_check_detail;
DROP TABLE IF EXISTS booklist_check_task;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS purchased_problem_books;
DROP TABLE IF EXISTS publisher_whitelist;
DROP TABLE IF EXISTS problem_books;
DROP TABLE IF EXISTS sensitive_words;
DROP TABLE IF EXISTS sensitive_categories;
DROP TABLE IF EXISTS sys_user;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 第四部分：创建数据表
-- =====================================================

-- -----------------------------------------------------
-- 表 1: sys_user (用户信息表)
-- 说明: 存储系统用户信息，支持管理员和普通用户两种角色
-- -----------------------------------------------------
CREATE TABLE sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID（主键，自增）',
    username VARCHAR(50) NOT NULL COMMENT '用户名（用于登录，必须唯一）',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值（使用 BCrypt 加密）',
    employee_id VARCHAR(50) COMMENT '员工工号（可选，用于与人事系统关联）',
    role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '用户角色：admin-管理员, user-普通用户',
    department VARCHAR(100) COMMENT '所属部门',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    is_active TINYINT(1) DEFAULT 1 COMMENT '账号状态：1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    
    -- 唯一约束
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_employee_id (employee_id),
    
    -- 普通索引
    INDEX idx_role (role),
    INDEX idx_is_active (is_active),
    INDEX idx_department (department),
    
    -- 检查约束（MySQL 8.0.16+ 支持）
    CONSTRAINT chk_role CHECK (role IN ('admin', 'user')),
    CONSTRAINT chk_is_active CHECK (is_active IN (0, 1))
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户信息表 - 存储系统用户账号信息';


-- -----------------------------------------------------
-- 表 2: sensitive_categories (敏感词分类表)
-- 说明: 敏感词的分类管理，如：政治敏感、违法违规、暴力色情等
-- -----------------------------------------------------
CREATE TABLE sensitive_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID（主键，自增）',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称（如：政治敏感、违法违规）',
    description VARCHAR(200) COMMENT '分类描述说明',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '最后更新人用户ID',
    update_time DATETIME COMMENT '最后更新时间',
    
    -- 唯一约束
    UNIQUE INDEX uk_category_name (category_name),
    
    -- 外键约束
    CONSTRAINT fk_sc_created_by FOREIGN KEY (created_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    CONSTRAINT fk_sc_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='敏感词分类表 - 管理敏感词的类别';


-- -----------------------------------------------------
-- 表 3: sensitive_words (敏感词表)
-- 说明: 存储具体的敏感词条目，关联到敏感词分类
-- -----------------------------------------------------
CREATE TABLE sensitive_words (
    word_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '敏感词ID（主键，自增）',
    category_id BIGINT NOT NULL COMMENT '所属分类ID（外键）',
    keyword VARCHAR(100) NOT NULL COMMENT '敏感关键词（最长100字符）',
    match_type TINYINT NOT NULL DEFAULT 1 COMMENT '匹配类型：0-精确匹配, 1-模糊匹配, 2-正则表达式',
    risk_level TINYINT NOT NULL DEFAULT 2 COMMENT '风险等级：1-低风险, 2-中风险, 3-高风险',
    detection_type VARCHAR(20) NOT NULL DEFAULT '关键词' COMMENT '检测范围：关键词-全局检测, 书名-仅检测书名, 作者-仅检测作者',
    alert_message VARCHAR(200) COMMENT '匹配时的警告提示信息',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用, 0-禁用',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '最后更新人用户ID',
    update_time DATETIME COMMENT '最后更新时间',
    
    -- 索引
    INDEX idx_category_id (category_id),
    INDEX idx_keyword (keyword),
    INDEX idx_detection_type (detection_type),
    INDEX idx_is_active (is_active),
    INDEX idx_risk_level (risk_level),
    
    -- 全文索引（支持中文分词搜索）
    FULLTEXT INDEX ft_keyword (keyword) WITH PARSER ngram,
    
    -- 外键约束
    CONSTRAINT fk_sw_category FOREIGN KEY (category_id) REFERENCES sensitive_categories(category_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sw_created_by FOREIGN KEY (created_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    CONSTRAINT fk_sw_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    
    -- 检查约束
    CONSTRAINT chk_match_type CHECK (match_type IN (0, 1, 2)),
    CONSTRAINT chk_sw_risk_level CHECK (risk_level IN (1, 2, 3)),
    CONSTRAINT chk_detection_type CHECK (detection_type IN ('关键词', '书名', '作者'))
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='敏感词表 - 存储需要检测的敏感关键词';


-- -----------------------------------------------------
-- 表 4: problem_books (问题图书库表)
-- 说明: 已知的问题图书记录，用于书单检测时匹配
-- -----------------------------------------------------
CREATE TABLE problem_books (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图书ID（主键，自增）',
    book_name VARCHAR(500) NOT NULL COMMENT '书名（标题）',
    author VARCHAR(200) COMMENT '作者',
    isbn VARCHAR(20) COMMENT 'ISBN 国际标准书号',
    publisher VARCHAR(200) COMMENT '出版社',
    publish_year VARCHAR(4) COMMENT '出版年份（格式：YYYY）',
    problem_type VARCHAR(100) COMMENT '问题类型（如：内容问题、版权问题）',
    source VARCHAR(255) COMMENT '问题来源（如：教育部通报、读者举报）',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '最后更新人用户ID',
    update_time DATETIME COMMENT '最后更新时间',
    
    -- 索引
    INDEX idx_isbn (isbn),
    INDEX idx_book_name (book_name(255)),
    INDEX idx_author (author),
    INDEX idx_publisher (publisher),
    INDEX idx_problem_type (problem_type),
    
    -- 全文索引（支持书名的中文搜索）
    FULLTEXT INDEX ft_book_name (book_name) WITH PARSER ngram,
    
    -- 外键约束
    CONSTRAINT fk_pb_created_by FOREIGN KEY (created_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    CONSTRAINT fk_pb_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='问题图书库表 - 存储已知的问题图书信息';


-- -----------------------------------------------------
-- 表 5: publisher_whitelist (出版社白名单表)
-- 说明: 正规出版社名单，用于检测非白名单出版社的图书
-- -----------------------------------------------------
CREATE TABLE publisher_whitelist (
    publisher_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出版社ID（主键，自增）',
    publisher_name VARCHAR(200) NOT NULL COMMENT '出版社名称',
    years BIGINT COMMENT '年份批次（用于区分不同年份的白名单）',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用, 0-禁用',
    created_by BIGINT NOT NULL COMMENT '创建人用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 唯一约束
    UNIQUE INDEX uk_publisher_name (publisher_name),
    
    -- 索引
    INDEX idx_is_active (is_active),
    INDEX idx_years (years),
    
    -- 外键约束
    CONSTRAINT fk_pw_created_by FOREIGN KEY (created_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='出版社白名单表 - 存储正规出版社名单';


-- -----------------------------------------------------
-- 表 6: purchased_problem_books (已采购问题图书表)
-- 说明: 记录已经采购入库的问题图书，用于追踪处理状态
-- -----------------------------------------------------
CREATE TABLE purchased_problem_books (
    asset_code BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资产编码（主键，自增）',
    system_id VARCHAR(20) COMMENT '来源系统ID：system_1-Aleph系统, system_2-超星系统',
    isbn VARCHAR(20) COMMENT 'ISBN 国际标准书号',
    book_name VARCHAR(500) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    publisher VARCHAR(200) COMMENT '出版社',
    purchase_date DATE COMMENT '采购日期',
    location VARCHAR(100) COMMENT '存放位置/馆藏地',
    problem_reason TEXT COMMENT '问题原因说明',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '处理状态：pending-待处理, processing-处理中, processed-已处理, reinstated-已恢复',
    handler_id BIGINT COMMENT '处理人用户ID',
    handler_date DATETIME COMMENT '处理日期',
    
    -- 索引
    INDEX idx_isbn (isbn),
    INDEX idx_status (status),
    INDEX idx_system_id (system_id),
    INDEX idx_book_name (book_name(255)),
    INDEX idx_purchase_date (purchase_date),
    
    -- 外键约束
    CONSTRAINT fk_ppb_handler FOREIGN KEY (handler_id) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    
    -- 检查约束
    CONSTRAINT chk_system_id CHECK (system_id IN ('system_1', 'system_2') OR system_id IS NULL),
    CONSTRAINT chk_status CHECK (status IN ('pending', 'processing', 'processed', 'reinstated'))
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='已采购问题图书表 - 记录已入库的问题图书及处理状态';


-- -----------------------------------------------------
-- 表 7: operation_log (操作日志表)
-- 说明: 记录系统中所有重要操作的日志，用于审计追踪
-- -----------------------------------------------------
CREATE TABLE operation_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID（主键，自增）',
    module VARCHAR(50) NOT NULL COMMENT '操作模块（如：user, sensitive_word, problem_book）',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：create-创建, update-更新, delete-删除',
    target_id BIGINT COMMENT '操作目标记录ID',
    old_value TEXT COMMENT '修改前的数据（JSON格式）',
    new_value TEXT COMMENT '修改后的数据（JSON格式）',
    operated_by BIGINT NOT NULL COMMENT '操作人用户ID',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_address VARCHAR(45) COMMENT '操作者IP地址（支持IPv6）',
    
    -- 索引
    INDEX idx_module (module),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operated_by (operated_by),
    INDEX idx_operation_time (operation_time),
    INDEX idx_target_id (target_id),
    
    -- 外键约束
    CONSTRAINT fk_log_operated_by FOREIGN KEY (operated_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    
    -- 检查约束
    CONSTRAINT chk_operation_type CHECK (operation_type IN ('create', 'update', 'delete'))
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='操作日志表 - 记录系统操作审计日志';


-- -----------------------------------------------------
-- 表 8: booklist_check_task (书目检测任务表)
-- 说明: 存储书单检测任务信息，每次上传书单检测创建一条任务记录
-- -----------------------------------------------------
CREATE TABLE booklist_check_task (
    task_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID（主键，自增）',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称（格式：提交人_日期_序号）',
    task_type VARCHAR(50) COMMENT '任务类型：批量检测、单本检测',
    submitted_by BIGINT NOT NULL COMMENT '提交人用户ID',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    start_time DATETIME COMMENT '任务开始执行时间',
    end_time DATETIME COMMENT '任务完成时间',
    original_filename VARCHAR(500) COMMENT '上传的原始文件名',
    file_path VARCHAR(500) COMMENT '文件在服务器上的存储路径',
    result_file_path VARCHAR(500) COMMENT '检测结果文件路径',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '任务状态：pending-待处理, processing-处理中, success-成功, failed-失败, cancelled-已取消',
    error_message TEXT COMMENT '错误信息（任务失败时记录）',
    sensitive_hits INT DEFAULT 0 COMMENT '敏感词命中数量',
    problem_book_hits INT DEFAULT 0 COMMENT '问题图书命中数量',
    non_whitelist_pubs INT DEFAULT 0 COMMENT '非白名单出版社数量',
    total_books INT DEFAULT 0 COMMENT '检测的图书总数',
    total_problem_books INT DEFAULT 0 COMMENT '检出的问题图书总数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time DATETIME COMMENT '记录更新时间',
    
    -- 索引
    INDEX idx_submitted_by (submitted_by),
    INDEX idx_submit_time (submit_time),
    INDEX idx_status (status),
    INDEX idx_task_type (task_type),
    
    -- 外键约束
    CONSTRAINT fk_task_submitted_by FOREIGN KEY (submitted_by) REFERENCES sys_user(user_id) ON UPDATE CASCADE,
    
    -- 检查约束
    CONSTRAINT chk_task_status CHECK (status IN ('pending', 'processing', 'success', 'failed', 'cancelled'))
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='书目检测任务表 - 存储书单检测任务信息';


-- -----------------------------------------------------
-- 表 9: booklist_check_detail (书目检测详情表)
-- 说明: 存储检测任务中每本书的详细检测结果
-- -----------------------------------------------------
CREATE TABLE booklist_check_detail (
    detail_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '详情ID（主键，自增）',
    task_id BIGINT NOT NULL COMMENT '所属任务ID（外键）',
    book_number VARCHAR(50) COMMENT '图书编号/序号',
    book_name VARCHAR(500) COMMENT '书名（标题）',
    subtitle VARCHAR(200) COMMENT '副标题',
    author1 VARCHAR(100) COMMENT '第一作者',
    author2 VARCHAR(100) COMMENT '第二作者',
    author VARCHAR(100) COMMENT '合并作者（兼容字段）',
    isbn VARCHAR(20) COMMENT 'ISBN 国际标准书号',
    publish_location VARCHAR(100) COMMENT '出版地',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date VARCHAR(50) COMMENT '出版日期',
    target_audience VARCHAR(100) COMMENT '目标读者',
    content_summary TEXT COMMENT '内容摘要/简介',
    classification_number VARCHAR(50) COMMENT '分类号',
    language VARCHAR(50) COMMENT '语言',
    hit_sensitive TINYINT DEFAULT 0 COMMENT '是否命中敏感词：1-是, 0-否',
    hit_problem_book TINYINT DEFAULT 0 COMMENT '是否命中问题图书：1-是, 0-否',
    is_whitelist_publisher TINYINT DEFAULT 1 COMMENT '是否白名单出版社：1-是, 0-否',
    risk_level VARCHAR(20) DEFAULT 'low' COMMENT '风险等级：high-高, medium-中, low-低',
    sensitive_words TEXT COMMENT '命中的敏感词详情（JSON格式）',
    detection_time DATETIME COMMENT '检测时间',
    check_status VARCHAR(20) DEFAULT 'pending' COMMENT '检查状态：pending-待检查, completed-已完成, error-检查出错',
    error_message TEXT COMMENT '错误信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_time DATETIME COMMENT '记录更新时间',
    
    -- 索引
    INDEX idx_task_id (task_id),
    INDEX idx_isbn (isbn),
    INDEX idx_book_name (book_name(255)),
    INDEX idx_publisher (publisher),
    INDEX idx_risk_level (risk_level),
    INDEX idx_check_status (check_status),
    INDEX idx_hit_sensitive (hit_sensitive),
    INDEX idx_hit_problem_book (hit_problem_book),
    
    -- 外键约束（级联删除：删除任务时自动删除关联的检测详情）
    CONSTRAINT fk_detail_task FOREIGN KEY (task_id) REFERENCES booklist_check_task(task_id) ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- 检查约束
    CONSTRAINT chk_hit_sensitive CHECK (hit_sensitive IN (0, 1)),
    CONSTRAINT chk_hit_problem_book CHECK (hit_problem_book IN (0, 1)),
    CONSTRAINT chk_is_whitelist_publisher CHECK (is_whitelist_publisher IN (0, 1)),
    CONSTRAINT chk_detail_risk_level CHECK (risk_level IN ('high', 'medium', 'low')),
    CONSTRAINT chk_check_status CHECK (check_status IN ('pending', 'completed', 'error'))
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='书目检测详情表 - 存储每本书的检测结果';


-- =====================================================
-- 第五部分：验证数据库结构
-- =====================================================

-- 显示所有已创建的表
SELECT 
    TABLE_NAME AS '表名',
    TABLE_COMMENT AS '表说明',
    ENGINE AS '存储引擎',
    TABLE_COLLATION AS '字符集'
FROM 
    information_schema.TABLES 
WHERE 
    TABLE_SCHEMA = 'library_management'
ORDER BY 
    TABLE_NAME;

-- =====================================================
-- 建表脚本执行完成
-- =====================================================
-- 
-- 已创建以下 9 张表:
-- 1. sys_user              - 用户信息表
-- 2. sensitive_categories  - 敏感词分类表
-- 3. sensitive_words       - 敏感词表
-- 4. problem_books         - 问题图书库表
-- 5. publisher_whitelist   - 出版社白名单表
-- 6. purchased_problem_books - 已采购问题图书表
-- 7. operation_log         - 操作日志表
-- 8. booklist_check_task   - 书目检测任务表
-- 9. booklist_check_detail - 书目检测详情表
--
-- 下一步：执行 02_mysql_init_data.sql 填充初始数据
-- =====================================================

