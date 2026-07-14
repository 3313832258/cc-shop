package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticsStatus {
    CREATED(0, "已下单"),
    SHIPPED(1, "已发货"),
    IN_TRANSIT(2, "运输中"),
    DELIVERING(3, "派送中"),
    DELIVERED(4, "已签收");

    private final int code;
    private final String desc;
}
