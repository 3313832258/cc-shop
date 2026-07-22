package com.ccshop.admin.controller;

import com.ccshop.admin.service.AdminCustomerService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/customer")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 客户管理")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping("/list")
    @Operation(summary = "客户列表")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminCustomerService.listCustomers(page, size, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "客户详情（含订单统计）")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(adminCustomerService.getCustomerDetail(id));
    }
}
