-- =====================================================
-- Library Problem Books Management System - Initial Data (MySQL Chinese Version)
-- =====================================================
-- Database: MySQL 8.0+
-- Created: 2025-11-06
-- Description: Initialize base data for the system (Chinese)
-- =====================================================

-- Set character encoding
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE library_management;

-- =====================================================
-- 1. Initialize Super Admin Account
-- =====================================================
-- Password: admin123 (BCrypt encrypted)
-- IMPORTANT: Change default password in production environment
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('admin', '$2a$10$5.gKc63lignU4z5Bt24vkegg4YgvpJn0T1LFfpM9wz8t6QGl9qwcm', 'admin', '系统管理部', '系统管理员', 'ADMIN001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. Initialize Test User Account (Optional, can be removed in production)
-- =====================================================
-- Password: user123
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('testuser', '$2a$10$LYF.8/AoEyfueP0lAMg/0.4gmup0uXn5f3lomCZqvSRITAeaHTxp2', 'user', '图书管理部', '测试用户', 'USER001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 3. Initialize Sensitive Word Categories
-- =====================================================
INSERT INTO sensitive_categories (category_name, description, created_by, create_time)
VALUES
    ('违法违规', '违反法律法规的内容', 1, CURRENT_TIMESTAMP),
    ('政治敏感', '涉及政治敏感内容', 1, CURRENT_TIMESTAMP),
    ('暴力色情', '暴力或色情相关内容', 1, CURRENT_TIMESTAMP),
    ('作者', '敏感作者名单', 1, CURRENT_TIMESTAMP),
    ('出版社', '敏感出版社名单', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 4. Initialize Sample Sensitive Words (Optional)
-- =====================================================
INSERT INTO sensitive_words (category_id, keyword, match_type, risk_level, detection_type, is_active, created_by, create_time)
VALUES
    (1, '暴力', 1, 3, '关键词', 1, 1, CURRENT_TIMESTAMP),
    (2, '政治敏感词', 1, 3, '关键词', 1, 1, CURRENT_TIMESTAMP),
    (3, '色情内容', 1, 3, '关键词', 1, 1, CURRENT_TIMESTAMP),
    (1, '违法', 1, 2, '关键词', 1, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 5. Initialize Publisher Whitelist
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
    ('商务印书馆', 1, 1, CURRENT_TIMESTAMP),
    ('中华书局', 1, 1, CURRENT_TIMESTAMP),
    ('上海译文出版社', 1, 1, CURRENT_TIMESTAMP),
    ('三联书店', 1, 1, CURRENT_TIMESTAMP),
    ('中国社会科学出版社', 1, 1, CURRENT_TIMESTAMP),
    ('法律出版社', 1, 1, CURRENT_TIMESTAMP),
    ('人民教育出版社', 1, 1, CURRENT_TIMESTAMP),
    ('化学工业出版社', 1, 1, CURRENT_TIMESTAMP),
    ('人民卫生出版社', 1, 1, CURRENT_TIMESTAMP),
    ('中国建筑工业出版社', 1, 1, CURRENT_TIMESTAMP),
    ('经济科学出版社', 1, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 6. Initialize Sample Problem Books
-- =====================================================
INSERT INTO problem_books (isbn, book_name, author, publisher, publish_year, problem_type, source, created_by, create_time)
VALUES
    ('9787111111111', '示例问题图书1', '示例作者1', '示例出版社', '2020', '内容问题', '教育部通报', 1, CURRENT_TIMESTAMP),
    ('9787222222222', '示例问题图书2', '示例作者2', '问题出版社', '2019', '版权问题', '出版社通知', 1, CURRENT_TIMESTAMP),
    ('9787333333333', '示例问题图书3', '示例作者3', '某某出版社', '2021', '内容问题', '读者举报', 1, CURRENT_TIMESTAMP),
    ('9787444444444', '示例问题图书4', '示例作者4', '另一出版社', '2022', '质量问题', '质检部门反馈', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- Important Notes
-- =====================================================
-- 1. The above data is for development and testing only
-- 2. Remove or modify test data when deploying to production
-- 3. Change the super admin password immediately after first login
-- 4. BCrypt password format: $2a$10$[52 character hash]
-- 5. Default passwords: 
--    - admin / admin123
--    - testuser / user123
-- 6. Boolean values use 1 (true) and 0 (false) in MySQL
-- =====================================================

