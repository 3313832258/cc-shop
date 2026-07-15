package com.ccshop.trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private Long userCouponId;
}
