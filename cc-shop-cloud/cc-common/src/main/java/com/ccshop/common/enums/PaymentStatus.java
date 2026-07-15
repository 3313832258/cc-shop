package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING(0, "待支付"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;

    public static PaymentStatus fromCode(int code) {
        for (PaymentStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown PaymentStatus: " + code);
    }
}
