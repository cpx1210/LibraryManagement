-- 更新 admin 用户密码（正确的 BCrypt 哈希）
-- 用户名：admin
-- 密码（明文）：admin123
-- 此哈希已验证正确

UPDATE sys_user
SET password_hash = '$2a$10$RJ6XUimipOxX.ubyVVSkqeLfs9zAU6KTGvVZUXFIlWaP..8zkh98O'
WHERE username = 'admin';

-- 验证更新
SELECT
    user_id,
    username,
    role,
    is_active,
    length(password_hash) as hash_length,
    password_hash
FROM sys_user
WHERE username = 'admin';
