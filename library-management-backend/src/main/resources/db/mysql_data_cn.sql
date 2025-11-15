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
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin', '系统管理部', '系统管理员', 'ADMIN001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. Initialize Test User Account (Optional, can be removed in production)
-- =====================================================
-- Password: user123
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('testuser', '$2a$10$rFxWQxHhQIzVqkAEv8LqxuXJ5QwQX5fZ8wqGwqHqQX5fZ8wqGwqH', 'user', '图书管理部', '测试用户', 'USER001', 1, CURRENT_TIMESTAMP);

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
-- Note: Commented out for security, add as needed
-- INSERT INTO sensitive_words (category_id, keyword, match_type, risk_level, detection_type, is_active, created_by, create_time)
-- VALUES
--     (1, '示例关键词1', 1, 3, '关键词', 1, 1, CURRENT_TIMESTAMP),
--     (2, '示例关键词2', 1, 3, '关键词', 1, 1, CURRENT_TIMESTAMP);

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

