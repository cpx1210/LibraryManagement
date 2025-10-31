-- =====================================================
-- Library Problem Books Management System - Initial Data
-- =====================================================
-- Database: PostgreSQL 15.x
-- Updated: 2025-10-30
-- Description: Initialize base data for the system
-- =====================================================

-- Set client encoding to UTF8
SET client_encoding = 'UTF8';

-- =====================================================
-- 1. Initialize Super Admin Account
-- =====================================================
-- Password: admin123 (BCrypt encrypted)
-- IMPORTANT: Change default password in production environment
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin', 'System Admin', 'Administrator', 'ADMIN001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. Initialize Test User Account (Optional, can be removed in production)
-- =====================================================
-- Password: user123
INSERT INTO sys_user (username, password_hash, role, department, real_name, employee_id, is_active, create_time)
VALUES ('testuser', '$2a$10$rFxWQxHhQIzVqkAEv8LqxuXJ5QwQX5fZ8wqGwqHqQX5fZ8wqGwqH', 'user', 'Library Dept', 'Test User', 'USER001', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 3. Initialize Sensitive Word Categories
-- =====================================================
INSERT INTO sensitive_categories (category_name, description, created_by, create_time)
VALUES
    ('Illegal', 'Content violating laws and regulations', 1, CURRENT_TIMESTAMP),
    ('Political', 'Politically sensitive content', 1, CURRENT_TIMESTAMP),
    ('Violence', 'Violence or pornography related content', 1, CURRENT_TIMESTAMP),
    ('Author', 'Sensitive authors list', 1, CURRENT_TIMESTAMP),
    ('Publisher', 'Sensitive publishers list', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 4. Initialize Sample Sensitive Words (Optional)
-- =====================================================
-- Note: Add actual sensitive words as needed
-- INSERT INTO sensitive_words (category_id, keyword, match_type, risk_level, is_active, created_by, create_time)
-- VALUES
--     (1, 'keyword1', 1, 3, true, 1, CURRENT_TIMESTAMP),
--     (2, 'keyword2', 1, 3, true, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 5. Initialize Sample Publisher Whitelist (Optional)
-- =====================================================
INSERT INTO publisher_whitelist (publisher_name, is_active, created_by, create_time)
VALUES
    ('People Publishing House', true, 1, CURRENT_TIMESTAMP),
    ('Higher Education Press', true, 1, CURRENT_TIMESTAMP),
    ('Tsinghua University Press', true, 1, CURRENT_TIMESTAMP),
    ('Peking University Press', true, 1, CURRENT_TIMESTAMP),
    ('Machinery Industry Press', true, 1, CURRENT_TIMESTAMP),
    ('Electronics Industry Press', true, 1, CURRENT_TIMESTAMP),
    ('Science Press', true, 1, CURRENT_TIMESTAMP),
    ('Renmin University Press', true, 1, CURRENT_TIMESTAMP),
    ('Foreign Language Teaching and Research Press', true, 1, CURRENT_TIMESTAMP),
    ('Commercial Press', true, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- Important Notes
-- =====================================================
-- 1. The above data is for development and testing only
-- 2. Remove or modify test data when deploying to production
-- 3. Change the super admin password immediately
-- 4. BCrypt password format: $2a$10$[52 character hash]
-- 5. Default passwords: admin123 and user123
-- =====================================================
