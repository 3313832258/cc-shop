package com.ccshop.common.jwt;

import com.ccshop.common.core.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具
 */
public class JwtUtil {

    /**
     * 密钥，优先从环境变量 JWT_SECRET 读取，其次从系统属性，最后使用默认值（仅限开发环境）。
     * 长度需 >= 256 bit。
     */
    private static final String SECRET = System.getenv("JWT_SECRET") != null
            ? System.getenv("JWT_SECRET")
            : System.getProperty("jwt.secret", "ccshop-secret-key-for-jwt-signing-please-change-in-prod-2026");

    /** AccessToken 有效期 2 小时 */
    public static final long ACCESS_EXPIRATION_MS = 2L * 60 * 60 * 1000;
    /** RefreshToken 有效期 7 天 */
    public static final long REFRESH_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    private static SecretKey key() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 AccessToken（短期，2h）
     */
    public static String generate(Long userId, String username, int role) {
        return generate(userId, username, role, ACCESS_EXPIRATION_MS);
    }

    /**
     * 生成指定有效期的 Token
     */
    public static String generate(Long userId, String username, int role, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim(Constants.CLAIM_USER_ID, userId)
                .claim(Constants.CLAIM_USERNAME, username)
                .claim(Constants.CLAIM_ROLE, role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key())
                .compact();
    }

    public static Claims parse(String token) {
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        Claims c = parse(token);
        Object id = c.get(Constants.CLAIM_USER_ID);
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        return id != null ? Long.valueOf(id.toString()) : null;
    }

    public static String getUsername(String token) {
        return parse(token).get(Constants.CLAIM_USERNAME, String.class);
    }
}
