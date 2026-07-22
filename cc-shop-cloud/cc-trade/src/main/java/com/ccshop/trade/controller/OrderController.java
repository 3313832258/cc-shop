package com.ccshop.trade.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.trade.dto.OrderVO;
import com.ccshop.trade.dto.PlaceOrderRequest;
import com.ccshop.trade.service.IdempotentService;
import com.ccshop.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trade/order")
@RequiredArgsConstructor
@Tag(name = "订单")
public class OrderController {

    private final OrderService orderService;
    private final IdempotentService idempotentService;

    @GetMapping("/idempotent-token")
    @Operation(summary = "获取幂等Token（下单前调用）")
    public Result<String> getIdempotentToken() {
        Long userId = UserContext.getUserId();
        return Result.success(idempotentService.generateToken(userId));
    }

    @PostMapping("/place")
    @Operation(summary = "下单")
    public Result<OrderVO> place(@Valid @RequestBody PlaceOrderRequest req) {
        Long userId = UserContext.getUserId();

        // 验证幂等 Token
        if (!idempotentService.verifyAndConsume(userId, req.getIdempotentToken())) {
            return Result.fail("请勿重复提交订单");
        }

        return Result.success(orderService.placeOrder(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrder(id));
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public Result<List<OrderVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(orderService.listOrders(status, keyword, page, size));
    }
}
