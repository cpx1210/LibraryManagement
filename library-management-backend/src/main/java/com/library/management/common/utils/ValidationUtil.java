package com.library.management.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 数据校验工具类
 *
 * 功能说明：
 * 1. 手机号格式校验
 * 2. 邮箱格式校验
 * 3. 身份证号校验（18位，含校验码）
 * 4. 用户名校验
 * 5. 密码强度校验
 * 6. 通用校验方法
 *
 * @author Library Management System
 * @since 2025-10-14
 */
@Slf4j
public class ValidationUtil {

    // ==================== 正则表达式常量 ====================

    /**
     * 手机号正则：1开头，第二位为3-9，共11位数字
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则：标准邮箱格式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,6}$"
    );

    /**
     * 用户名正则：4-20位，字母、数字、下划线
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

    /**
     * 密码正则：至少包含字母和数字，8-20位
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,20}$"
    );

    /**
     * 身份证号正则：18位数字或17位数字+X
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

    // ==================== 手机号校验 ====================

    /**
     * 校验手机号格式
     *
     * @param mobile 手机号
     * @return true 表示格式正确
     */
    public static boolean isMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    // ==================== 邮箱校验 ====================

    /**
     * 校验邮箱格式
     *
     * @param email 邮箱
     * @return true 表示格式正确
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ==================== 身份证校验 ====================

    /**
     * 校验身份证号（18位，含校验码验证）
     *
     * 算法说明：
     * 1. 前17位是数字
     * 2. 第18位是校验码（数字或X）
     * 3. 校验码计算：前17位每位乘以权重，求和后对11取模，根据余数确定校验码
     *
     * @param idCard 身份证号
     * @return true 表示有效
     */
    public static boolean isIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }

        String cleanIdCard = idCard.trim().toUpperCase();

        // 格式校验
        if (!ID_CARD_PATTERN.matcher(cleanIdCard).matches()) {
            return false;
        }

        // 校验码验证
        try {
            // 权重因子
            int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
            // 校验码对照表
            char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

            // 计算加权和
            int sum = 0;
            for (int i = 0; i < 17; i++) {
                sum += (cleanIdCard.charAt(i) - '0') * weights[i];
            }

            // 计算校验码
            int mod = sum % 11;
            char expectedCheckCode = checkCodes[mod];

            // 比较校验码
            return cleanIdCard.charAt(17) == expectedCheckCode;

        } catch (Exception e) {
            log.error("身份证号校验失败: {}", idCard, e);
            return false;
        }
    }

    // ==================== 用户名校验 ====================

    /**
     * 校验用户名格式
     *
     * 规则：4-20位，只能包含字母、数字、下划线
     *
     * @param username 用户名
     * @return true 表示格式正确
     */
    public static boolean isUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    // ==================== 密码强度校验 ====================

    /**
     * 校验密码强度
     *
     * 规则：8-20位，至少包含字母和数字，可包含特殊字符 @$!%*#?&
     *
     * @param password 密码
     * @return true 表示符合强度要求
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 校验密码最小长度
     *
     * @param password  密码
     * @param minLength 最小长度
     * @return true 表示满足最小长度
     */
    public static boolean isPasswordMinLength(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }

    // ==================== 通用校验 ====================

    /**
     * 非空校验
     *
     * @param str 字符串
     * @return true 表示非空
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 长度校验
     *
     * @param str       字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return true 表示长度在范围内
     */
    public static boolean isLengthInRange(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 纯数字校验
     *
     * @param str 字符串
     * @return true 表示全是数字
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }

    /**
     * 纯字母校验
     *
     * @param str 字符串
     * @return true 表示全是字母
     */
    public static boolean isAlpha(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return str.matches("[a-zA-Z]+");
    }

    /**
     * 字母和数字组合校验
     *
     * @param str 字符串
     * @return true 表示只包含字母和数字
     */
    public static boolean isAlphanumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return str.matches("[a-zA-Z0-9]+");
    }

    /**
     * URL 格式校验
     *
     * @param url URL字符串
     * @return true 表示格式正确
     */
    public static boolean isUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        String urlPattern = "^(http|https)://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$";
        return url.matches(urlPattern);
    }

    /**
     * IP 地址格式校验
     *
     * @param ip IP地址
     * @return true 表示格式正确
     */
    public static boolean isIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        String ipPattern = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return ip.matches(ipPattern);
    }

    /**
     * 整数范围校验
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return true 表示在范围内
     */
    public static boolean isIntegerInRange(Integer value, int min, int max) {
        return value != null && value >= min && value <= max;
    }

    /**
     * 中文字符校验
     *
     * @param str 字符串
     * @return true 表示全是中文
     */
    public static boolean isChinese(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return str.matches("[\\u4e00-\\u9fa5]+");
    }

    /**
     * 校验字符串是否包含中文
     *
     * @param str 字符串
     * @return true 表示包含中文
     */
    public static boolean containsChinese(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return Pattern.compile("[\\u4e00-\\u9fa5]").matcher(str).find();
    }
}
