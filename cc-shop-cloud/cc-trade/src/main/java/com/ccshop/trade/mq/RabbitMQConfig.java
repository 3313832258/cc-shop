package com.ccshop.trade.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ==================== 订单超时 ====================
    public static final String ORDER_DLX = "order.dlx";
    public static final String ORDER_DELAY_QUEUE = "order.delay";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout";

    // ==================== 支付成功 ====================
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success";
    public static final String PAYMENT_SUCCESS_DLQ = "payment.success.dlq";

    // ==================== 优惠券核销 ====================
    public static final String COUPON_USE_QUEUE = "coupon.use";
    public static final String COUPON_USE_DLQ = "coupon.use.dlq";

    // ==================== 死信 Exchange ====================
    public static final String COMMON_DLX = "common.dlx";

    @Bean
    public DirectExchange orderDlx() {
        return new DirectExchange(ORDER_DLX);
    }

    @Bean
    public DirectExchange commonDlx() {
        return new DirectExchange(COMMON_DLX);
    }

    // ==================== 订单延迟队列 ====================
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .withArgument("x-message-ttl", 30 * 60 * 1000)
                .withArgument("x-dead-letter-exchange", ORDER_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_TIMEOUT_QUEUE)
                .build();
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderDlx())
                .with(ORDER_TIMEOUT_QUEUE);
    }

    // ==================== 支付成功队列（带死信） ====================
    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", COMMON_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_SUCCESS_DLQ)
                .build();
    }

    @Bean
    public Queue paymentSuccessDlq() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_DLQ).build();
    }

    @Bean
    public Binding paymentSuccessDlqBinding() {
        return BindingBuilder.bind(paymentSuccessDlq())
                .to(commonDlx())
                .with(PAYMENT_SUCCESS_DLQ);
    }

    // ==================== 优惠券核销队列（带死信） ====================
    @Bean
    public Queue couponUseQueue() {
        return QueueBuilder.durable(COUPON_USE_QUEUE)
                .withArgument("x-dead-letter-exchange", COMMON_DLX)
                .withArgument("x-dead-letter-routing-key", COUPON_USE_DLQ)
                .build();
    }

    @Bean
    public Queue couponUseDlq() {
        return QueueBuilder.durable(COUPON_USE_DLQ).build();
    }

    @Bean
    public Binding couponUseDlqBinding() {
        return BindingBuilder.bind(couponUseDlq())
                .to(commonDlx())
                .with(COUPON_USE_DLQ);
    }
}
