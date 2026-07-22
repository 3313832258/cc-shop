package com.ccshop.admin.controller;

import com.ccshop.admin.annotation.OperationLog;
import com.ccshop.admin.service.AdminOrderService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 订单管理")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(adminOrderService.listOrders(page, size, status, keyword, startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.success(adminOrderService.getOrder(id));
    }

    @PutMapping("/{id}/ship")
    @OperationLog(module = "order", operationType = "UPDATE", description = "订单发货")
    public Result<Void> ship(@PathVariable Long id) {
        adminOrderService.shipOrder(id);
        return Result.success();
    }
}
