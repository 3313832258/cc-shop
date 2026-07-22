package com.ccshop.user.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.tracker.UserActionEvent;
import com.ccshop.user.mq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/action")
@RequiredArgsConstructor
public class ActionController {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 批量接收用户行为事件，发送到 MQ 异步落库
     * 前端批量上报，减少网络请求
     */
    @PostMapping("/batch")
    public Result<Void> batchReport(@RequestBody List<UserActionEvent> events) {
        if (events == null || events.isEmpty()) {
            return Result.success();
        }

        log.info("收到 {} 条用户行为事件", events.size());

        // 逐条发送到 MQ（保证消息可靠性）
        for (UserActionEvent event : events) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.USER_ACTION_EXCHANGE,
                        RabbitMQConfig.USER_ACTION_ROUTING_KEY,
                        event
                );
            } catch (Exception e) {
                log.warn("发送用户行为事件到MQ失败: action={}, userId={}, error={}",
                        event.getAction(), event.getUserId(), e.getMessage());
                // 单条失败不影响其他事件
            }
        }

        return Result.success();
    }

    /**
     * 单条上报（备用接口）
     */
    @PostMapping("/report")
    public Result<Void> report(@RequestBody UserActionEvent event) {
        log.debug("收到用户行为事件: action={}, userId={}", event.getAction(), event.getUserId());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_ACTION_EXCHANGE,
                    RabbitMQConfig.USER_ACTION_ROUTING_KEY,
                    event
            );
            return Result.success();
        } catch (Exception e) {
            log.warn("发送用户行为事件到MQ失败: action={}, userId={}, error={}",
                    event.getAction(), event.getUserId(), e.getMessage());
            return Result.fail("上报失败");
        }
    }
}
