package com.ccshop.trade.dto;

import lombok.Data;

@Data
public class AftersaleApplyRequest {
    private Long orderId;
    private Long orderItemId;
    /** refund=仅退款, return_refund=退货退款 */
    private String type;
    private String reason;
}
