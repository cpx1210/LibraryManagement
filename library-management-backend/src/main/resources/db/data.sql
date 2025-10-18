-- 图书馆问题图书管理系统 - 初始化数据脚本
-- 数据库: PostgreSQL 15.x（通用SQL语法，兼容MySQL）
-- 更新日期: 2025-10-14

-- =====================================================
-- 1. 初始化超级管理员账号
-- =====================================================
-- 密码: admin123 (使用BCrypt加密)
-- 注意：生产环境请务必修改默认密码
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin', '系统管理部', '系统管理员', 'ADMIN001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. 初始化测试用户账号（可选，生产环境可删除）
-- =====================================================
-- 密码: user123
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('testuser', '$2a$10$rFxWQxHhQIzVqkAEv8LqxuXJ5QwQX5fZ8wqGwqHqQX5fZ8wqGwqH', 'user', '图书管理部', '测试用户', 'USER001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 3. 初始化敏感词分类
-- =====================================================
INSERT INTO sensitive_categories (category_name, description, created_by, create_time)
VALUES
    ('违法违规', '违反法律法规的内容', 1, CURRENT_TIMESTAMP),
    ('政治敏感', '涉及政治敏感内容', 1, CURRENT_TIMESTAMP),
    ('暴力色情', '暴力或色情相关内容', 1, CURRENT_TIMESTAMP),
    ('作者', '敏感作者名单', 1, CURRENT_TIMESTAMP),
    ('出版社', '敏感出版社名单', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 4. 初始化一些示例敏感词（可选）
-- =====================================================
INSERT INTO sensitive_words (category_id, keyword, match_type, risk_level, is_active, created_by, create_time)
VALUES
    (1, '暴力', 1, 3, 1, 1, CURRENT_TIMESTAMP),
    (1, '色情', 1, 3, 1, 1, CURRENT_TIMESTAMP),
    (2, '邪教', 1, 3, 1, 1, CURRENT_TIMESTAMP),
    (2, '分裂', 1, 2, 1, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 5. 初始化示例出版社白名单（可选）
-- =====================================================
INSERT INTO publisher_whitelist (publisher_name, is_active, created_by, create_time)
VALUES
    ('人民出版社', 1, 1, CURRENT_TIMESTAMP),
    ('高等教育出版社', 1, 1, CURRENT_TIMESTAMP),
    ('清华大学出版社', 1, 1, CURRENT_TIMESTAMP),
    ('北京大学出版社', 1, 1, CURRENT_TIMESTAMP),
    ('机械工业出版社', 1, 1, CURRENT_TIMESTAMP),
    ('电子工业出版社', 1, 1, CURRENT_TIMESTAMP),
    ('科学出版社', 1, 1, CURRENT_TIMESTAMP),
    ('中国人民大学出版社', 1, 1, CURRENT_TIMESTAMP),
    ('外语教学与研究出版社', 1, 1, CURRENT_TIMESTAMP),
    ('商务印书馆', 1, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 注意事项
-- =====================================================
-- 1. 以上数据仅供开发测试使用
-- 2. 生产环境部署时应删除或修改测试数据
-- 3. 超级管理员密码务必修改
-- 4. BCrypt 加密的密码格式：$2a$10$[52个字符的哈希值]
-- 5. 默认密码：admin123 和 user123
