package com.library.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 图书馆管理系统 - 启动类
 * 作用：
 * - 这是整个应用的入口点
 * - Spring Boot会从这里启动，扫描com.library.management包下的所有组件
 * - 自动配置数据库连接、Web服务器等
 */
@SpringBootApplication
@MapperScan("com.library.management.module.*.mapper")
public class LibraryManagementApplication {
    public static void main(String[] args) {
        // 启动Spring Boot应用
        // 会自动启动内置的Tomcat服务器（默认8080端口）
        SpringApplication.run(LibraryManagementApplication.class, args);

        System.out.println("\n========================================");
        System.out.println("  图书馆管理系统启动成功！");
        System.out.println("  访问地址：http://localhost:8080/api");
        System.out.println("  API文档：http://localhost:8080/api/doc.html");
        System.out.println("========================================\n");
    }
}
