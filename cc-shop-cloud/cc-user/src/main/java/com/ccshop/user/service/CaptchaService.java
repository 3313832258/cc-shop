package com.ccshop.user.service;

import com.ccshop.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 数学验证码服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_TTL_MINUTES = 5;

    /**
     * 生成数学验证码，返回 {captchaId, expression}
     */
    public Map<String, String> generate() {
        int a = ThreadLocalRandom.current().nextInt(1, 50);
        int b = ThreadLocalRandom.current().nextInt(1, 50);
        String[] ops = {"+", "-", "×"};
        String op = ops[ThreadLocalRandom.current().nextInt(ops.length)];

        int answer;
        switch (op) {
            case "+": answer = a + b; break;
            case "-":
                // 保证结果非负
                if (a < b) { int t = a; a = b; b = t; }
                answer = a - b;
                break;
            default: answer = a * b; break;
        }

        String expression = a + " " + op + " " + b + " = ?";
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        redisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaId,
                String.valueOf(answer),
                CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES
        );

        return Map.of("captchaId", captchaId, "expression", expression);
    }

    /**
     * 校验验证码（验证成功后删除）
     */
    public void verify(String captchaId, String answer) {
        if (captchaId == null || captchaId.isBlank() || answer == null || answer.isBlank()) {
            throw new BusinessException(400, "验证码不能为空");
        }
        String redisKey = CAPTCHA_PREFIX + captchaId;
        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached == null) {
            throw new BusinessException(400, "验证码已过期，请刷新");
        }
        if (!cached.trim().equals(answer.trim())) {
            throw new BusinessException(400, "验证码错误");
        }
        redisTemplate.delete(redisKey);
    }
}
