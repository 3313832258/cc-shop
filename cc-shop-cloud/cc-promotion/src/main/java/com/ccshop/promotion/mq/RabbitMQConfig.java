package com.ccshop.promotion.mq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * 队列声明在 cc-trade 的 RabbitMQConfig 中统一管理，此处仅配置消费者所需的 MessageConverter
 */
@Configuration
public class RabbitMQConfig {

    public static final String COUPON_USE_QUEUE = "coupon.use";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
