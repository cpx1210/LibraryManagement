package com.library.management.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 *
 * 作用：使用 BCrypt 算法对密码进行加密和验证
 *
 * BCrypt 算法说明：
 * - 单向加密算法，无法解密
 * - 每次加密同一个密码，结果都不同（内置随机盐）
 * - 专门为密码设计的慢哈希函数，可防止暴力破解
 * - Spring Security 推荐使用的密码加密方式
 *
 * 使用场景：
 * 1. 用户注册时加密密码
 * 2. 用户登录时验证密码
 * 3. 修改密码时加密新密码
 * 4. 重置密码时加密默认密码
 *
 * 安全说明：
 * - 不要使用 MD5、SHA1 等快速哈希算法加密密码
 * - BCrypt 会自动生成随机盐，不需要手动管理盐值
 * - 加密后的密码长度固定为 60 个字符
 */
public class PasswordUtil {

    /**
     * BCrypt 密码编码器
     * 说明：这是一个线程安全的单例对象，可以在多线程环境中使用
     */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     *
     * 工作原理：
     * 1. BCrypt 自动生成随机盐
     * 2. 将盐和密码结合后进行哈希
     * 3. 返回包含盐和哈希值的加密字符串
     *
     * @param rawPassword 原始密码（明文）
     * @return 加密后的密码（60个字符的BCrypt格式字符串）
     *
     * 示例：
     * String encrypted = PasswordUtil.encode("123456");
     * // 结果类似：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * 工作原理：
     * 1. 从加密密码中提取盐
     * 2. 使用相同的盐对原始密码进行加密
     * 3. 比较两次加密的结果是否一致
     *
     * @param rawPassword 原始密码（明文，用户输入的密码）
     * @param encodedPassword 加密密码（存储在数据库中的密码）
     * @return true-密码匹配，false-密码不匹配
     *
     * 示例：
     * String encrypted = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM";
     * boolean matches = PasswordUtil.matches("123456", encrypted);
     * // 如果 encrypted 是由 "123456" 加密而来，matches 为 true
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成随机密码
     *
     * 说明：用于重置密码、批量创建用户等场景
     * 生成规则：8位随机字符（包含数字和字母）
     *
     * @return 随机密码
     *
     * 示例：
     * String randomPassword = PasswordUtil.generateRandomPassword();
     * // 结果类似：aB3dE7fH
     */
    public static String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    /**
     * 私有构造函数，防止实例化
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
