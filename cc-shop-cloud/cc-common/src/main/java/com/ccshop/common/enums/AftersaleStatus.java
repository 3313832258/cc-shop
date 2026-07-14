package com.ccshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AftersaleStatus {
    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    PROCESSING(3, "处理中"),
    COMPLETED(4, "已完成");

    private final int code;
    private final String desc;
}
