package com.library.management.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * ISBN 工具类
 *
 * 功能说明：
 * 1. 支持 ISBN-10 和 ISBN-13 两种格式
 * 2. 格式校验（长度、字符、校验码）
 * 3. ISBN-10 与 ISBN-13 互相转换
 * 4. 格式化（添加/移除连字符）
 *
 * ISBN-10 格式：X-XXX-XXXXX-X (总共10位，最后一位可能是X)
 * ISBN-13 格式：XXX-X-XXX-XXXXX-X (总共13位，前缀978或979)
 *
 * @author Library Management System
 * @since 2025-10-14
 */
@Slf4j
public class IsbnUtil {

    /**
     * 校验 ISBN 是否有效（支持 ISBN-10 和 ISBN-13）
     *
     * @param isbn ISBN 字符串（可以包含连字符）
     * @return true 表示有效，false 表示无效
     */
    public static boolean isValid(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        // 移除连字符和空格
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");

        // 根据长度判断 ISBN 类型
        if (cleanIsbn.length() == 10) {
            return isValidIsbn10(cleanIsbn);
        } else if (cleanIsbn.length() == 13) {
            return isValidIsbn13(cleanIsbn);
        }

        return false;
    }

    /**
     * 校验 ISBN-10 格式
     *
     * 算法说明：
     * ISBN-10 的校验码计算方法：
     * 1. 前9位数字分别乘以 10, 9, 8, 7, 6, 5, 4, 3, 2
     * 2. 将所有乘积相加
     * 3. 总和除以 11，取余数
     * 4. 用 11 减去余数，得到校验码（如果结果是10，用X表示；
     如果是11，用0表示）
     *
     * @param isbn 10位 ISBN（不含连字符）
     * @return true 表示有效
     */
    public static boolean isValidIsbn10(String isbn) {
        if (isbn == null || isbn.length() != 10) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                char c = isbn.charAt(i);
                if (!Character.isDigit(c)) {
                    return false;
                }
                int digit = c - '0';
                sum += digit * (10 - i);
            }

            // 处理最后一位校验码（可能是数字或X）
            char lastChar = isbn.charAt(9);
            int checkDigit;
            if (lastChar == 'X' || lastChar == 'x') {
                checkDigit = 10;
            } else if (Character.isDigit(lastChar)) {
                checkDigit = lastChar - '0';
            } else {
                return false;
            }

            sum += checkDigit;

            // 校验：总和必须是 11 的倍数
            return sum % 11 == 0;

        } catch (Exception e) {
            log.error("ISBN-10 校验失败: {}", isbn, e);
            return false;
        }
    }

    /**
     * 校验 ISBN-13 格式
     *
     * 算法说明：
     * ISBN-13 的校验码计算方法：
     * 1. 前12位数字，奇数位乘以1，偶数位乘以3
     * 2. 将所有乘积相加
     * 3. 总和除以 10，取余数
     * 4. 用 10
     减去余数，得到校验码（如果结果是10，则校验码为0）
     *
     * @param isbn 13位 ISBN（不含连字符）
     * @return true 表示有效
     */
    public static boolean isValidIsbn13(String isbn) {
        if (isbn == null || isbn.length() != 13) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                char c = isbn.charAt(i);
                if (!Character.isDigit(c)) {
                    return false;
                }
                int digit = c - '0';

                sum += (i % 2 == 0) ? digit : digit * 3;
            }

            // 处理最后一位校验码
            char lastChar = isbn.charAt(12);
            if (!Character.isDigit(lastChar)) {
                return false;
            }
            int checkDigit = lastChar - '0';

            // 计算正确的校验码
            int calculatedCheckDigit = (10 - (sum % 10)) % 10;

            return checkDigit == calculatedCheckDigit;

        } catch (Exception e) {
            log.error("ISBN-13 校验失败: {}", isbn, e);
            return false;
        }
    }

    /**
     * 将 ISBN-10 转换为 ISBN-13
     *
     * 转换规则：
     * 1. 在 ISBN-10 的前9位前面加上前缀 "978"
     * 2. 重新计算校验码（使用 ISBN-13 的算法）
     *
     * @param isbn10 ISBN-10 字符串
     * @return ISBN-13 字符串，如果输入无效则返回 null
     */
    public static String convertIsbn10To13(String isbn10) {
        if (isbn10 == null) {
            return null;
        }

        // 移除连字符和空格
        String cleanIsbn10 = isbn10.replaceAll("[\\s-]",
                "").toUpperCase();

        if (!isValidIsbn10(cleanIsbn10)) {
            log.warn("无效的 ISBN-10 格式: {}", isbn10);
            return null;
        }

        // 取前9位，加上前缀978
        String isbn13Prefix = "978" + cleanIsbn10.substring(0,
                9);

        // 计算 ISBN-13 的校验码
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn13Prefix.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;

        return isbn13Prefix + checkDigit;
    }

    /**
     * 将 ISBN-13 转换为 ISBN-10
     *
     * 转换规则：
     * 1. 只有前缀为 "978" 的 ISBN-13 才能转换为 ISBN-10
     * 2. 去掉前缀 "978"，取后面的9位
     * 3. 重新计算校验码（使用 ISBN-10 的算法）
     *
     * @param isbn13 ISBN-13 字符串
     * @return ISBN-10 字符串，如果输入无效或不支持转换则返回
    null
     */
    public static String convertIsbn13To10(String isbn13) {
        if (isbn13 == null) {
            return null;
        }

        // 移除连字符和空格
        String cleanIsbn13 = isbn13.replaceAll("[\\s-]", "");

        if (!isValidIsbn13(cleanIsbn13)) {
            log.warn("无效的 ISBN-13 格式: {}", isbn13);
            return null;
        }

        // 只有前缀为978的ISBN-13才能转换为ISBN-10
        if (!cleanIsbn13.startsWith("978")) {
            log.warn("ISBN-13 必须以 978 开头才能转换为ISBN-10: {}", isbn13);
            return null;
        }

        // 取第4-12位（去掉978前缀和最后的校验码）
        String isbn10Prefix = cleanIsbn13.substring(3, 12);

        // 计算 ISBN-10 的校验码
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = isbn10Prefix.charAt(i) - '0';
            sum += digit * (10 - i);
        }
        int checkDigit = (11 - (sum % 11)) % 11;

        // 如果校验码是10，用X表示
        String checkChar = (checkDigit == 10) ? "X" :
                String.valueOf(checkDigit);

        return isbn10Prefix + checkChar;
    }

    /**
     * 标准化 ISBN 格式（移除所有连字符和空格，统一转为大写）
     *
     * @param isbn 原始 ISBN 字符串
     * @return 标准化后的 ISBN（纯数字或包含X）
     */
    public static String normalize(String isbn) {
        if (isbn == null) {
            return null;
        }
        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    /**
     * 格式化 ISBN（添加连字符，便于阅读）
     *
     * ISBN-10 格式：X-XXX-XXXXX-X
     * ISBN-13 格式：XXX-X-XXX-XXXXX-X
     *
     * @param isbn 原始 ISBN 字符串
     * @return 格式化后的 ISBN，如果输入无效则返回原字符串
     */
    public static String format(String isbn) {
        if (isbn == null) {
            return null;
        }

        String cleanIsbn = normalize(isbn);

        if (cleanIsbn.length() == 10 &&
                isValidIsbn10(cleanIsbn)) {
            // 格式：X-XXX-XXXXX-X
            return cleanIsbn.substring(0, 1) + "-" +
                    cleanIsbn.substring(1, 4) + "-" +
                    cleanIsbn.substring(4, 9) + "-" +
                    cleanIsbn.substring(9, 10);
        } else if (cleanIsbn.length() == 13 &&
                isValidIsbn13(cleanIsbn)) {
            // 格式：XXX-X-XXX-XXXXX-X
            return cleanIsbn.substring(0, 3) + "-" +
                    cleanIsbn.substring(3, 4) + "-" +
                    cleanIsbn.substring(4, 7) + "-" +
                    cleanIsbn.substring(7, 12) + "-" +
                    cleanIsbn.substring(12, 13);
        }

        // 如果无效，返回原字符串
        return isbn;
    }

    /**
     * 判断是否为 ISBN-10 格式
     *
     * @param isbn ISBN 字符串
     * @return true 表示是 ISBN-10
     */
    public static boolean isIsbn10(String isbn) {
        if (isbn == null) {
            return false;
        }
        String cleanIsbn = normalize(isbn);
        return cleanIsbn.length() == 10 &&
                isValidIsbn10(cleanIsbn);
    }

    /**
     * 判断是否为 ISBN-13 格式
     *
     * @param isbn ISBN 字符串
     * @return true 表示是 ISBN-13
     */
    public static boolean isIsbn13(String isbn) {
        if (isbn == null) {
            return false;
        }
        String cleanIsbn = normalize(isbn);
        return cleanIsbn.length() == 13 &&
                isValidIsbn13(cleanIsbn);
    }
}