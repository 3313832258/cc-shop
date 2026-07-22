package com.ccshop.trade.mq;

import com.ccshop.trade.service.LogisticsSimulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotifyConsumer {

    private final LogisticsSimulator logisticsSimulator;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(Map<String, Object> message) {
        Long orderId = Long.valueOf(message.get("orderId").toString());
        String paymentNo = message.get("paymentNo") != null ? message.get("paymentNo").toString() : "N/A";
        log.info("支付成功通知: orderId={}, paymentNo={}", orderId, paymentNo);

        // 创建物流记录
        try {
            logisticsSimulator.createLogistics(orderId);
            log.info("物流记录创建成功: orderId={}", orderId);
        } catch (Exception e) {
            log.error("创建物流记录失败: orderId={}", orderId, e);
        }
    }
}
