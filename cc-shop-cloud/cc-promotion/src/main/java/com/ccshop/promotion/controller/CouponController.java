package com.ccshop.promotion.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.promotion.dto.CouponVO;
import com.ccshop.promotion.dto.MyCouponVO;
import com.ccshop.promotion.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotion/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/available")
    @Operation(summary = "可领取优惠券列表")
    public Result<List<CouponVO>> available() {
        return Result.success(couponService.getAvailable());
    }

    @PostMapping("/receive/{couponId}")
    @Operation(summary = "领取优惠券")
    public Result<Void> receive(@PathVariable Long couponId) {
        couponService.receive(UserContext.getUserId(), couponId);
        return Result.success();
    }

    @GetMapping("/my")
    @Operation(summary = "我的优惠券")
    public Result<List<MyCouponVO>> my(@RequestParam(required = false) Integer status) {
        return Result.success(couponService.getMyCoupons(UserContext.getUserId(), status));
    }
}
