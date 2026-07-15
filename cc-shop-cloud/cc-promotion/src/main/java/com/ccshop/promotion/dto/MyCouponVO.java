package com.ccshop.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MyCouponVO {
    private Long id;
    private Long couponId;
    private String name;
    /** 0=满减, 1=折扣 */
    private Integer type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private LocalDateTime endTime;
    /** 0=可用, 1=已用, 2=过期 */
    private Integer status;
    private LocalDateTime createdAt;
}
