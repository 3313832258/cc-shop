package com.ccshop.trade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AftersaleVO {
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private String type;
    private String typeText;
    private String reason;
    private Integer status;
    private String statusText;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
