package com.ccshop.user.service;

import com.ccshop.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务（Mock 模式：验证码存 Redis + 日志打印）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "sms:login:";
    private static final String LOCK_PREFIX = "sms:lock:";
    private static final int CODE_TTL_MINUTES = 5;
    private static final int LOCK_TTL_SECONDS = 60;

    /**
     * 发送登录验证码
     */
    public void sendLoginCode(String phone) {
        // 检查 60 秒防刷
        String lockKey = LOCK_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BusinessException(429, "验证码发送过于频繁，请稍后再试");
        }

        // 生成 6 位验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        // 存入 Redis
        String redisKey = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(lockKey, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);

        // Mock：日志打印验证码
        log.info("【Mock SMS】手机号 {} 的登录验证码为: {}，有效期 {} 分钟", phone, code, CODE_TTL_MINUTES);
    }

    /**
     * 校验验证码（验证成功后删除）
     */
    public boolean verifyCode(String phone, String code) {
        String redisKey = CODE_PREFIX + phone;
        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        if (!cached.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }
        // 验证成功，删除验证码
        redisTemplate.delete(redisKey);
        return true;
    }
}
