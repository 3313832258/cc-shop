package com.ccshop.promotion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务
 * 基于 Redis 实现，支持锁超时和防误删
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_VALUE_PREFIX = "lock_value:";

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的 Key
     * @param timeoutSeconds 锁超时时间（秒）
     * @return 锁的唯一标识，用于释放锁；null 表示获取失败
     */
    public String tryLock(String lockKey, long timeoutSeconds) {
        String value = UUID.randomUUID().toString();
        String redisKey = LOCK_PREFIX + lockKey;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, value, timeoutSeconds, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功: key={}, value={}", lockKey, value);
            return value;
        }

        log.debug("获取分布式锁失败: key={}", lockKey);
        return null;
    }

    /**
     * 释放分布式锁（使用 Lua 脚本保证原子性）
     * @param lockKey 锁的 Key
     * @param lockValue 锁的唯一标识
     */
    public void unlock(String lockKey, String lockValue) {
        String redisKey = LOCK_PREFIX + lockKey;

        // Lua 脚本：只有 value 匹配时才删除
        String luaScript = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
        """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(redisKey), lockValue);

        if (result != null && result > 0) {
            log.debug("释放分布式锁成功: key={}", lockKey);
        } else {
            log.warn("释放分布式锁失败（可能已过期或被其他线程持有）: key={}", lockKey);
        }
    }
}