package com.ccshop.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlashSaleVO {
    private Long id;
    private Long activityId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal flashPrice;
    private Integer totalStock;
    private Integer availableStock;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer activityStatus;
}
