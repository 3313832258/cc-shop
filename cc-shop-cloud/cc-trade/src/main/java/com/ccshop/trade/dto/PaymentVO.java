package com.ccshop.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private Long id;
    private Long orderId;
    private String paymentNo;
    private BigDecimal amount;
    private String method;
    private Integer status;
    private String statusDesc;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
