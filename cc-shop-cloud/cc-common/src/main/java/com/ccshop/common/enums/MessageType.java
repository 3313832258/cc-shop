package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型：order 订单 / promotion 促销 / system 系统
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    ORDER("order", "订单消息"),
    PROMOTION("promotion", "促销消息"),
    SYSTEM("system", "系统消息");

    private final String code;
    private final String desc;
}
