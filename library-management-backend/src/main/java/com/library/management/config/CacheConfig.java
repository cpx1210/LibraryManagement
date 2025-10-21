package com.library.management.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 *
 * 功能说明：
 * 1. 启用 Spring Cache 缓存机制
 * 2. 配置 Caffeine 作为缓存实现
 * 3. 定义缓存策略（过期时间、最大容量等）
 *
 * 使用场景：
 * - 敏感词库缓存：用于高频的敏感词检测，避免每次都查询数据库
 * - 其他字典数据缓存：如问题书目、出版社信息等
 *
 * 注解说明：
 * - @Configuration：标记为配置类
 * - @EnableCaching：启用 Spring Cache 缓存支持
 *
 * @author Library Management System
 * @since 2025-10-21
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存管理器
     *
     * 配置说明：
     * - initialCapacity：初始容量 100 条
     * - maximumSize：最大容量 10000 条（超过后按 LRU 策略淘汰）
     * - expireAfterWrite：写入后 1 小时过期
     * - recordStats：记录缓存统计信息（命中率、加载时间等）
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 初始容量：100 条
                .initialCapacity(100)
                // 最大容量：10000 条（足够存储所有敏感词）
                .maximumSize(10000)
                // 写入后 1 小时过期（敏感词更新频率低，1小时足够）
                .expireAfterWrite(1, TimeUnit.HOURS)
                // 记录缓存统计信息
                .recordStats());
        return cacheManager;
    }
}
