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
     * 密钥（生产环境应从配置中心读取，此处为简历项目演示用固定值）。
     * 长度需 >= 256 bit。
     */
    private static final String SECRET = "ccshop-secret-key-for-jwt-signing-please-change-in-prod-2026";

    /** 有效期 7 天 */
    private static final long EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    private static SecretKey key() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generate(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .claim(Constants.CLAIM_USER_ID, userId)
                .claim(Constants.CLAIM_USERNAME, username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_MS))
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
