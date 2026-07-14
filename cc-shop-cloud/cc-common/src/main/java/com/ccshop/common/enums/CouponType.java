package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型：满减固定金额 / 百分比折扣
 */
@Getter
@AllArgsConstructor
public enum CouponType {
    FIXED(0, "满减"),
    PERCENT(1, "折扣");

    private final int code;
    private final String desc;
}
