package com.ccshop.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponVO {
    private Long id;
    private String name;
    /** 0=满减, 1=折扣 */
    private Integer type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer remainingCount;
    /** 当前用户是否已领取 */
    private Boolean received;
}
