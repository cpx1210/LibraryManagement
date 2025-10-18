package com.library.management.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT (JSON Web Token) 工具类
 * 作用：生成、解析和验证 JWT 令牌
 *
 * JWT 说明：
 * - JWT 是一种用于用户认证的令牌格式
 * - 由三部分组成：Header（头部）、Payload（载荷）、Signature（签名）
 * - 格式：xxxxx.yyyyy.zzzzz
 * - 无状态认证：服务器不需要保存 session，可以水平扩展
 * 使用场景：
 * 1. 用户登录成功后生成 Token
 * 2. 前端请求时携带 Token 在 Authorization 请求头中
 * 3. 后端解析 Token 获取用户信息
 * 4. 验证 Token 是否有效
 * 安全说明：
 * - Secret 密钥必须保密，不能泄露
 * - Token 过期时间不宜过长（本项目设置为 2 小时）
 * - HTTPS 传输 Token，防止被窃取
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT 密钥
     * 从配置文件 application.yml 中读取 jwt.secret
     * 说明：用于签名和验证 Token
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token 过期时间（毫秒）
     * 从配置文件读取 jwt.expiration
     * 默认：7200000 毫秒（2 小时）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成密钥对象
     *
     * 说明：将配置文件中的字符串密钥转换为 SecretKey 对象
     * JJWT 0.12.x 版本要求使用 SecretKey 对象进行签名
     *
     * @return SecretKey 对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT Token 字符串
     *
     * Token 结构：
     * - Header：算法和令牌类型
     * - Payload：用户信息（username, userId, role）和过期时间
     * - Signature：签名
     *
     * 示例：
     * String token = jwtUtil.generateToken("admin", 1L, "admin");
     */
    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        claims.put("role", role);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)          // 签发时间
                .expiration(expiryDate) // 过期时间
                .signWith(getSigningKey()) // 签名
                .compact();
    }

    /**
     * 从 Token 中解析出所有声明（Claims）
     *
     * Claims 包含：
     * - username：用户名
     * - userId：用户ID
     * - role：用户角色
     * - iat：签发时间
     * - exp：过期时间
     * - sub：主题（username）
     *
     * @param token JWT Token
     * @return Claims 对象
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey()) // 验证签名
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名，解析失败返回 null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID，解析失败返回 null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
        }
        return null;
    }

    /**
     * 从 Token 中获取用户角色
     *
     * @param token JWT Token
     * @return 用户角色，解析失败返回 null
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 验证 Token 是否有效
     *
     * 验证规则：
     * 1. Token 能够正常解析
     * 2. Token 没有过期
     * 3. Token 中的用户名与传入的用户名一致
     *
     * @param token JWT Token
     * @param username 用户名
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null
                   && tokenUsername.equals(username)
                   && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证 Token 失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    private boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取 Token 过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}
