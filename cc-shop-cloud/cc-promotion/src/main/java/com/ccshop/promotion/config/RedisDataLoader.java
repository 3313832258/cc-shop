package com.ccshop.promotion.config;

import com.ccshop.common.core.Constants;
import com.ccshop.promotion.entity.Coupon;
import com.ccshop.promotion.mapper.CouponMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisDataLoader {

    private final CouponMapper couponMapper;
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void initCouponRemaining() {
        List<Coupon> coupons = couponMapper.selectList(null);
        for (Coupon c : coupons) {
            String key = Constants.COUPON_REMAINING + c.getId();
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, String.valueOf(c.getRemainingCount()));
            }
        }
    }
}
