package com.ccshop.admin.controller;

import com.ccshop.admin.service.AdminCouponService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 优惠券管理")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @GetMapping("/list")
    @Operation(summary = "优惠券列表")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminCouponService.listCoupons(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "优惠券详情")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.success(adminCouponService.getCoupon(id));
    }

    @PostMapping
    @Operation(summary = "新增优惠券")
    public Result<Long> create(@RequestBody Map<String, Object> req) {
        return Result.success(adminCouponService.createCoupon(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑优惠券")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminCouponService.updateCoupon(id, req);
        return Result.success();
    }

    @GetMapping("/{id}/claims")
    @Operation(summary = "优惠券领取/使用明细")
    public Result<Map<String, Object>> claims(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminCouponService.getCouponClaims(id, page, size, status));
    }
}
