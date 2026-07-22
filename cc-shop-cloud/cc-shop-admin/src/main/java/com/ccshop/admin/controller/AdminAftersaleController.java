package com.ccshop.admin.controller;

import com.ccshop.admin.annotation.OperationLog;
import com.ccshop.admin.service.AdminAftersaleService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/aftersale")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 售后管理")
public class AdminAftersaleController {

    private final AdminAftersaleService adminAftersaleService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminAftersaleService.listAftersales(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.success(adminAftersaleService.getAftersale(id));
    }

    @PutMapping("/{id}/approve")
    @OperationLog(module = "aftersale", operationType = "APPROVE", description = "售后审批通过")
    public Result<Void> approve(@PathVariable Long id) {
        adminAftersaleService.approve(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @OperationLog(module = "aftersale", operationType = "REJECT", description = "售后审批拒绝")
    public Result<Void> reject(@PathVariable Long id) {
        adminAftersaleService.reject(id);
        return Result.success();
    }
}
