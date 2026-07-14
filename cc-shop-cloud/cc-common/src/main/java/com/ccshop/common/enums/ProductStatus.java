package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    DRAFT(0, "下架"),
    ON_SALE(1, "上架"),
    OFF_SHELF(2, "停售");

    private final int code;
    private final String desc;
}
