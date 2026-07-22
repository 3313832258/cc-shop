package com.ccshop.promotion.controller;

import com.ccshop.common.core.Result;
import com.ccshop.promotion.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内部接口，供其他微服务通过 Feign 调用，不经过 Gateway
 */
@RestController
@RequestMapping("/promotion/internal/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券-内部")
public class InternalCouponController {

    private final CouponService couponService;

    @GetMapping("/{userCouponId}")
    @Operation(summary = "查询优惠券详情（供交易服务调用）")
    public Result<CouponService.CouponInfoDTO> getCouponDetail(@PathVariable Long userCouponId) {
        return Result.success(couponService.getCouponDetail(userCouponId));
    }

    @PostMapping("/restore")
    @Operation(summary = "恢复优惠券（供超时取消调用）")
    public Result<Void> restore(@RequestBody Map<String, Object> req) {
        Long userId = Long.valueOf(req.get("userId").toString());
        Long userCouponId = Long.valueOf(req.get("userCouponId").toString());
        couponService.restoreCoupon(userId, userCouponId);
        return Result.success();
    }
}
