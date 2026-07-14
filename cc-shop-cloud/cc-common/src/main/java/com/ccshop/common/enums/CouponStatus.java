package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponStatus {
    AVAILABLE(0, "可用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期");

    private final int code;
    private final String desc;
}
