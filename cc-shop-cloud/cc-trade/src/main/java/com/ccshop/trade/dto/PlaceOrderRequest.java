package com.ccshop.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private Long userCouponId;

    /**
     * 幂等 Token，用于防止重复提交
     * 前端通过 GET /trade/order/idempotent-token 获取
     */
    @NotBlank(message = "幂等Token不能为空")
    private String idempotentToken;
}
