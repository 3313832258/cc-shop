package com.ccshop.admin.controller;

import com.ccshop.admin.annotation.OperationLog;
import com.ccshop.admin.service.AdminProductService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 商品管理")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminProductService.listProducts(page, size, keyword, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.success(adminProductService.getProduct(id));
    }

    @PostMapping
    @OperationLog(module = "product", operationType = "CREATE", description = "新增商品")
    public Result<Long> create(@RequestBody Map<String, Object> req) {
        return Result.success(adminProductService.createProduct(req));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "product", operationType = "UPDATE", description = "编辑商品")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminProductService.updateProduct(id, req);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "product", operationType = "UPDATE", description = "商品上下架", targetIdParam = "id")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminProductService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.success(adminProductService.getCategories());
    }

    @GetMapping("/brands")
    public Result<List<Map<String, Object>>> brands() {
        return Result.success(adminProductService.getBrands());
    }
}
