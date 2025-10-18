package com.library.management;

import com.library.management.common.utils.PasswordUtil;

/**
 * 密码哈希生成工具
 * 用于生成 BCrypt 密码哈希，方便插入测试数据
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        // 生成测试密码的 BCrypt 哈希
        String password = "admin123";
        String hash = PasswordUtil.encode(password);

        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("哈希长度: " + hash.length());
        System.out.println();
        System.out.println("SQL更新语句:");
        System.out.println("UPDATE sys_user SET password_hash = '" + hash + "' WHERE username = 'admin';");
    }
}
