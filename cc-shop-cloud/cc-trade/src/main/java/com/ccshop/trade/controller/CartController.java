package com.ccshop.trade.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.trade.dto.CartItemRequest;
import com.ccshop.trade.dto.CartItemVO;
import com.ccshop.trade.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trade/cart")
@RequiredArgsConstructor
@Tag(name = "购物车")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车")
    public Result<Void> add(@Valid @RequestBody CartItemRequest req) {
        cartService.addItem(UserContext.getUserId(), req);
        return Result.success();
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品数量")
    public Result<Void> update(@Valid @RequestBody CartItemRequest req) {
        cartService.updateQuantity(UserContext.getUserId(), req);
        return Result.success();
    }

    @DeleteMapping("/remove/{skuId}")
    @Operation(summary = "移除购物车商品")
    public Result<Void> remove(@PathVariable Long skuId) {
        cartService.removeItem(UserContext.getUserId(), skuId);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取购物车列表")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.getList(UserContext.getUserId()));
    }

    @PutMapping("/select")
    @Operation(summary = "切换选中状态")
    public Result<Void> select(@RequestBody CartItemRequest req) {
        cartService.toggleSelect(UserContext.getUserId(), req);
        return Result.success();
    }
}
