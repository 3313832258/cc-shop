package com.ccshop.promotion.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COUPON_USE_QUEUE = "coupon.use";

    @Bean
    public Queue couponUseQueue() {
        return QueueBuilder.durable(COUPON_USE_QUEUE).build();
    }
}
