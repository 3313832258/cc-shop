package com.ccshop.trade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性服务
 * 用于防止重复提交（下单、支付等关键接口）
 */
@Service
@RequiredArgsConstructor
public class IdempotentService {

    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "idempotent:token:";
    private static final String USED_PREFIX = "idempotent:used:";
    /** Token 有效期 30 分钟 */
    private static final long TOKEN_TTL_MINUTES = 30;
    /** 已使用标记保留 1 小时（用于防重放） */
    private static final long USED_TTL_HOURS = 1;

    /**
     * 生成幂等 Token
     * @return UUID 格式的 Token
     */
    public String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String redisKey = TOKEN_PREFIX + userId + ":" + token;
        redisTemplate.opsForValue().set(redisKey, "1", TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 验证并消费幂等 Token
     * @param userId 用户 ID
     * @param token 幂等 Token
     * @return true=首次请求，false=重复请求
     */
    public boolean verifyAndConsume(Long userId, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String tokenKey = TOKEN_PREFIX + userId + ":" + token;
        String usedKey = USED_PREFIX + userId + ":" + token;

        // 检查是否已使用过
        if (Boolean.TRUE.equals(redisTemplate.hasKey(usedKey))) {
            return false;
        }

        // 尝试删除 Token（原子操作）
        Boolean deleted = redisTemplate.delete(tokenKey);
        if (Boolean.TRUE.equals(deleted)) {
            // 标记为已使用
            redisTemplate.opsForValue().set(usedKey, "1", USED_TTL_HOURS, TimeUnit.HOURS);
            return true;
        }

        return false;
    }
}