package com.ccshop.user.mq;

import com.ccshop.common.tracker.UserActionEvent;
import com.ccshop.user.service.UserActionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final UserActionLogService userActionLogService;

    /**
     * 消费用户行为事件，批量落库
     * 配置了死信队列，消费失败会进入死信队列 user.action.dlq
     */
    @RabbitListener(queues = RabbitMQConfig.USER_ACTION_QUEUE)
    public void handleUserAction(UserActionEvent event) {
        log.debug("收到用户行为事件: action={}, userId={}, targetType={}, targetId={}",
                event.getAction(), event.getUserId(), event.getTargetType(), event.getTargetId());

        try {
            userActionLogService.log(event);
            log.debug("用户行为落库成功: action={}, userId={}", event.getAction(), event.getUserId());
        } catch (Exception e) {
            log.error("用户行为落库失败: action={}, userId={}, error={}",
                    event.getAction(), event.getUserId(), e.getMessage(), e);
            throw e; // 抛出异常触发重试
        }
    }
}
