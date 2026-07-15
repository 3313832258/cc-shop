package com.ccshop.trade.feign;

import com.ccshop.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "cc-promotion", path = "/promotion")
public interface PromotionClient {

    @GetMapping("/internal/coupon/{userCouponId}")
    Result<CouponInfoDTO> getCouponInfo(@PathVariable("userCouponId") Long userCouponId);

    @PostMapping("/internal/coupon/restore")
    Result<Void> restoreCoupon(@RequestBody Map<String, Object> req);

    @lombok.Data
    class CouponInfoDTO {
        private Long userCouponId;
        private Long userId;
        private Integer type;
        private BigDecimal value;
        private BigDecimal minOrderAmount;
    }
}
