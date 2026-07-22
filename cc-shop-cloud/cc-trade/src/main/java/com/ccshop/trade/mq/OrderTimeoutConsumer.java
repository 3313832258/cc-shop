package com.ccshop.trade.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.enums.OrderStatus;
import com.ccshop.trade.dto.StockChangeRequest;
import com.ccshop.trade.entity.Order;
import com.ccshop.trade.entity.OrderItem;
import com.ccshop.trade.feign.ProductClient;
import com.ccshop.trade.feign.PromotionClient;
import com.ccshop.trade.mapper.OrderItemMapper;
import com.ccshop.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductClient productClient;
    private final PromotionClient promotionClient;

    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    public void handleOrderTimeout(Map<String, Object> message) {
        Long orderId = Long.valueOf(message.get("orderId").toString());
        log.info("订单超时检查: orderId={}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在: orderId={}", orderId);
            return;
        }

        // 非待付款状态则跳过
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            log.info("订单状态非待付款，跳过超时处理: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        // 更新订单为已取消
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.updateById(order);
        log.info("订单超时已取消: orderId={}", orderId);

        // 回滚库存
        try {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            StockChangeRequest req = new StockChangeRequest();
            req.setItems(items.stream().map(item -> {
                StockChangeRequest.StockItem si = new StockChangeRequest.StockItem();
                si.setSkuId(item.getSkuId());
                si.setQuantity(item.getQuantity());
                return si;
            }).collect(Collectors.toList()));
            var result = productClient.increaseStock(req);
            if (result != null && result.getCode() == 200) {
                log.info("库存回滚成功: orderId={}", orderId);
            } else {
                log.error("库存回滚失败: orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("库存回滚异常: orderId={}, error={}", orderId, e.getMessage(), e);
        }

        // 恢复优惠券
        if (order.getCouponId() != null) {
            try {
                Map<String, Object> req = new HashMap<>();
                req.put("userId", order.getUserId());
                req.put("userCouponId", order.getCouponId());
                promotionClient.restoreCoupon(req);
                log.info("优惠券恢复成功: orderId={}, userCouponId={}", orderId, order.getCouponId());
            } catch (Exception e) {
                log.error("优惠券恢复失败: orderId={}, error={}", orderId, e.getMessage(), e);
            }
        }
    }
}
