package com.ccshop.promotion.mq;

import com.ccshop.promotion.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUseConsumer {

    private final CouponService couponService;

    /**
     * 消费优惠券核销消息
     * 配置了死信队列，消费 3 次失败后会进入死信队列 coupon.use.dlq
     */
    @RabbitListener(queues = RabbitMQConfig.COUPON_USE_QUEUE)
    public void handleCouponUse(Map<String, Object> message) {
        Long userId = Long.valueOf(message.get("userId").toString());
        Long userCouponId = Long.valueOf(message.get("userCouponId").toString());
        Long orderId = Long.valueOf(message.get("orderId").toString());
        log.info("消费优惠券: userId={}, userCouponId={}, orderId={}", userId, userCouponId, orderId);

        try {
            couponService.useCoupon(userId, userCouponId, orderId);
            log.info("优惠券消费成功: userCouponId={}", userCouponId);
        } catch (Exception e) {
            log.error("优惠券消费失败，将重试: userCouponId={}, error={}", userCouponId, e.getMessage(), e);
            throw e; // 抛出异常触发重试
        }
    }
}
