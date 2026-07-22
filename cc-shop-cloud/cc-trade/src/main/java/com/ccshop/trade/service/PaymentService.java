package com.ccshop.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.UserContext;
import com.ccshop.common.enums.OrderStatus;
import com.ccshop.common.enums.PaymentStatus;
import com.ccshop.trade.dto.PaymentVO;
import com.ccshop.trade.entity.Order;
import com.ccshop.trade.entity.PaymentRecord;
import com.ccshop.trade.mapper.OrderMapper;
import com.ccshop.trade.mapper.PaymentRecordMapper;
import com.ccshop.trade.mq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PaymentVO pay(Long orderId, String method) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(401, "未登录");

        // 1. 校验订单
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new BusinessException(400, "订单状态不允许支付");
        }

        // 2. 幂等检查：是否已有成功支付记录
        PaymentRecord existRecord = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getStatus, PaymentStatus.SUCCESS.getCode()));
        if (existRecord != null) {
            log.info("重复支付请求，返回已有支付记录: orderId={}, paymentNo={}", orderId, existRecord.getPaymentNo());
            return toPaymentVO(existRecord);
        }

        // 3. 创建支付记录
        String paymentNo = "PY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setPaymentNo(paymentNo);
        record.setAmount(order.getFinalAmount());
        record.setMethod(method != null ? method : "alipay");
        record.setStatus(PaymentStatus.PENDING.getCode());
        paymentRecordMapper.insert(record);

        // 4. Mock 沙箱：3秒延迟，95% 成功率
        log.info("Mock 支付开始: orderId={}, paymentNo={}", orderId, paymentNo);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "支付中断");
        }

        boolean success = Math.random() < 0.95;

        if (success) {
            // 支付成功
            record.setStatus(PaymentStatus.SUCCESS.getCode());
            record.setPaidAt(LocalDateTime.now());
            paymentRecordMapper.updateById(record);

            order.setStatus(OrderStatus.PENDING_SHIPMENT.getCode());
            order.setPaidAt(record.getPaidAt());
            orderMapper.updateById(order);

            // MQ 通知
            Map<String, Object> msg = new HashMap<>();
            msg.put("orderId", order.getId());
            msg.put("paymentNo", paymentNo);
            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_SUCCESS_QUEUE, msg);
            log.info("支付成功: orderId={}, paymentNo={}, amount={}", orderId, paymentNo, record.getAmount());
        } else {
            // 支付失败
            record.setStatus(PaymentStatus.FAILED.getCode());
            paymentRecordMapper.updateById(record);
            log.info("支付失败(Mock): orderId={}, paymentNo={}", orderId, paymentNo);
        }

        return toPaymentVO(record);
    }

    private PaymentVO toPaymentVO(PaymentRecord record) {
        PaymentVO vo = new PaymentVO();
        vo.setId(record.getId());
        vo.setOrderId(record.getOrderId());
        vo.setPaymentNo(record.getPaymentNo());
        vo.setAmount(record.getAmount());
        vo.setMethod(record.getMethod());
        vo.setStatus(record.getStatus());
        vo.setStatusDesc(PaymentStatus.fromCode(record.getStatus()).getDesc());
        vo.setPaidAt(record.getPaidAt());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
