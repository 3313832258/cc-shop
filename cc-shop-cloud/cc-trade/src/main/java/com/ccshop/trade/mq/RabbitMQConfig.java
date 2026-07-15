package com.ccshop.trade.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_DLX = "order.dlx";
    public static final String ORDER_DELAY_QUEUE = "order.delay";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout";
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success";
    public static final String COUPON_USE_QUEUE = "coupon.use";

    @Bean
    public DirectExchange orderDlx() {
        return new DirectExchange(ORDER_DLX);
    }

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

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue couponUseQueue() {
        return QueueBuilder.durable(COUPON_USE_QUEUE).build();
    }
}
