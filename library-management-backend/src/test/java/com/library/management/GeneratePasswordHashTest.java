package com.library.management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class GeneratePasswordHashTest {

    @Test
    public void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String hash = encoder.encode(rawPassword);

        System.out.println("======================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("哈希长度: " + hash.length());
        System.out.println("======================================");
        System.out.println("SQL更新语句:");
        System.out.println("UPDATE sys_user SET password_hash = '" + hash + "' WHERE username = 'admin';");
        System.out.println("======================================");

        // 验证生成的哈希是否正确
        boolean matches = encoder.matches(rawPassword, hash);
        System.out.println("验证结果: " + (matches ? "✓ 成功" : "✗ 失败"));
    }
}
