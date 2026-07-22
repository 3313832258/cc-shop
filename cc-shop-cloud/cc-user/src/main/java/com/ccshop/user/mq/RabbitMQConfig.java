package com.ccshop.user.mq;

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

    // ==================== 用户行为埋点 ====================
    public static final String USER_ACTION_QUEUE = "user.action";
    public static final String USER_ACTION_EXCHANGE = "user.action.exchange";
    public static final String USER_ACTION_ROUTING_KEY = "user.action";
    public static final String USER_ACTION_DLQ = "user.action.dlq";

    // ==================== 死信 Exchange ====================
    public static final String COMMON_DLX = "common.dlx";

    @Bean
    public DirectExchange commonDlx() {
        return new DirectExchange(COMMON_DLX);
    }

    @Bean
    public DirectExchange userActionExchange() {
        return new DirectExchange(USER_ACTION_EXCHANGE);
    }

    @Bean
    public Queue userActionQueue() {
        return QueueBuilder.durable(USER_ACTION_QUEUE)
                .withArgument("x-dead-letter-exchange", COMMON_DLX)
                .withArgument("x-dead-letter-routing-key", USER_ACTION_DLQ)
                .build();
    }

    @Bean
    public Queue userActionDlq() {
        return QueueBuilder.durable(USER_ACTION_DLQ).build();
    }

    @Bean
    public Binding userActionBinding() {
        return BindingBuilder.bind(userActionQueue())
                .to(userActionExchange())
                .with(USER_ACTION_ROUTING_KEY);
    }

    @Bean
    public Binding userActionDlqBinding() {
        return BindingBuilder.bind(userActionDlq())
                .to(commonDlx())
                .with(USER_ACTION_DLQ);
    }
}
