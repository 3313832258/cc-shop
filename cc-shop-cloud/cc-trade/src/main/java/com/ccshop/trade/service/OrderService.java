package com.ccshop.trade.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.UserContext;
import com.ccshop.common.enums.CouponType;
import com.ccshop.common.enums.OrderStatus;
import com.ccshop.trade.dto.CartItemVO;
import com.ccshop.trade.dto.OrderVO;
import com.ccshop.trade.dto.PlaceOrderRequest;
import com.ccshop.trade.dto.StockChangeRequest;
import com.ccshop.trade.entity.Order;
import com.ccshop.trade.entity.OrderItem;
import com.ccshop.trade.feign.ProductClient;
import com.ccshop.trade.feign.PromotionClient;
import com.ccshop.trade.feign.UserClient;
import com.ccshop.trade.mapper.OrderItemMapper;
import com.ccshop.trade.mapper.OrderMapper;
import com.ccshop.trade.mq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final ProductClient productClient;
    private final PromotionClient promotionClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderVO placeOrder(PlaceOrderRequest req) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(401, "未登录");

        // 1. 读取购物车勾选项
        List<CartItemVO> cartItems = cartService.getList(userId);
        List<CartItemVO> selected = cartItems.stream()
                .filter(CartItemVO::getSelected)
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            throw new BusinessException(400, "请选择要购买的商品");
        }

        // 2. 批量扣库存（Feign → cc-product）
        StockChangeRequest stockReq = new StockChangeRequest();
        stockReq.setItems(selected.stream().map(item -> {
            StockChangeRequest.StockItem si = new StockChangeRequest.StockItem();
            si.setSkuId(item.getSkuId());
            si.setQuantity(item.getQuantity());
            return si;
        }).collect(Collectors.toList()));
        var stockResult = productClient.decreaseStock(stockReq);
        if (stockResult == null || stockResult.getCode() != 200) {
            throw new BusinessException(400, "库存不足，下单失败");
        }

        // 3. 计算金额
        BigDecimal totalAmount = selected.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        Long userCouponId = req.getUserCouponId();
        if (userCouponId != null) {
            var couponResult = promotionClient.getCouponInfo(userCouponId);
            if (couponResult != null && couponResult.getCode() == 200 && couponResult.getData() != null) {
                PromotionClient.CouponInfoDTO coupon = couponResult.getData();
                if (!coupon.getUserId().equals(userId)) {
                    throw new BusinessException(400, "优惠券不属于当前用户");
                }
                if (totalAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
                    throw new BusinessException(400, "未达到优惠券最低使用金额");
                }
                if (coupon.getType() == CouponType.FIXED.getCode()) {
                    discountAmount = coupon.getValue();
                } else if (coupon.getType() == CouponType.PERCENT.getCode()) {
                    // PERCENT: value 是折扣率，如 0.85 表示 85 折，折扣 = total * (1-value)
                    discountAmount = totalAmount.multiply(
                            BigDecimal.ONE.subtract(coupon.getValue()));
                }
                if (discountAmount.compareTo(totalAmount) > 0) {
                    discountAmount = totalAmount;
                }
            }
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // 4. 获取地址快照
        var addrResult = userClient.getAddress(req.getAddressId());
        if (addrResult == null || addrResult.getCode() != 200 || addrResult.getData() == null) {
            throw new BusinessException(400, "收货地址不存在");
        }
        String addressSnapshot = JSONUtil.toJsonStr(addrResult.getData());

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setCouponId(userCouponId);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setAddressSnapshot(addressSnapshot);
        orderMapper.insert(order);

        // 7. 创建订单明细
        for (CartItemVO item : selected) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(item.getProductId());
            oi.setSkuId(item.getSkuId());
            oi.setProductName(item.getProductName());
            oi.setSkuSpecs(JSONUtil.toJsonStr(item.getSpecs()));
            oi.setProductImage(item.getProductImage());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            orderItemMapper.insert(oi);
        }

        // 8. 清购物车
        for (CartItemVO item : selected) {
            cartService.removeItem(userId, item.getSkuId());
        }

        // 9. 事务提交后发 MQ
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (userCouponId != null) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("userId", userId);
                    msg.put("userCouponId", userCouponId);
                    msg.put("orderId", order.getId());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.COUPON_USE_QUEUE, msg);
                    log.info("已发送券核销消息: orderId={}, userCouponId={}", order.getId(), userCouponId);
                }
                Map<String, Object> timeoutMsg = new HashMap<>();
                timeoutMsg.put("orderId", order.getId());
                rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_DELAY_QUEUE, timeoutMsg);
                log.info("已发送超时延迟消息: orderId={}", order.getId());
            }
        });

        return toOrderVO(order);
    }

    public OrderVO getOrder(Long id) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }
        return toOrderVO(order);
    }

    public List<OrderVO> listOrders(Integer status, String keyword, int page, int size) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);
        if (status != null) {
            qw.eq(Order::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Order::getOrderNo, keyword);
        }
        Page<Order> p = orderMapper.selectPage(new Page<>(page, size), qw);
        return p.getRecords().stream().map(this::toOrderVO).collect(Collectors.toList());
    }

    private OrderVO toOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFinalAmount(order.getFinalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(OrderStatus.fromCode(order.getStatus()).getDesc());
        vo.setAddressSnapshot(order.getAddressSnapshot());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setPaidAt(order.getPaidAt());

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        List<OrderVO.OrderItemVO> itemVOs = items.stream().map(oi -> {
            OrderVO.OrderItemVO i = new OrderVO.OrderItemVO();
            i.setId(oi.getId());
            i.setProductId(oi.getProductId());
            i.setSkuId(oi.getSkuId());
            i.setProductName(oi.getProductName());
            i.setSkuSpecs(oi.getSkuSpecs());
            i.setProductImage(oi.getProductImage());
            i.setPrice(oi.getPrice());
            i.setQuantity(oi.getQuantity());
            return i;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);
        return vo;
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = 1000 + new Random().nextInt(9000);
        return "CC" + datePart + random;
    }
}
