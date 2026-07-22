package com.ccshop.product.service;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存降级服务
 * Redis 不可用时自动降级到数据库查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheFallbackService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 带降级的缓存查询
     * @param cacheKey 缓存 Key
     * @param ttlMinutes 缓存过期时间（分钟）
     * @param clazz 返回类型
     * @param dbFallback 数据库查询函数
     * @return 查询结果
     */
    public <T> T getWithFallback(String cacheKey, long ttlMinutes, Class<T> clazz, Supplier<T> dbFallback) {
        // 1. 尝试从缓存读取
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return JSONUtil.toBean(cached, clazz);
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级到数据库: key={}, error={}", cacheKey, e.getMessage());
        }

        // 2. 从数据库查询
        T result = dbFallback.get();

        // 3. 尝试写入缓存
        if (result != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), ttlMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis 写入失败，跳过缓存: key={}, error={}", cacheKey, e.getMessage());
            }
        }

        return result;
    }

    /**
     * 带降级的缓存删除
     */
    public void deleteWithFallback(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("Redis 删除失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * 检查 Redis 是否可用
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.hasKey("ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}