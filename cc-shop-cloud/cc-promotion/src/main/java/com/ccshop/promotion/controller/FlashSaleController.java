package com.ccshop.promotion.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.promotion.dto.FlashSaleVO;
import com.ccshop.promotion.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flash")
@RequiredArgsConstructor
@Tag(name = "秒杀")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/list")
    @Operation(summary = "秒杀商品列表")
    public Result<List<FlashSaleVO>> list() {
        return Result.success(flashSaleService.getFlashSaleItems());
    }

    @PostMapping("/buy/{itemId}")
    @Operation(summary = "秒杀抢购")
    public Result<Boolean> buy(@PathVariable Long itemId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        boolean success = flashSaleService.flashSale(userId, itemId);
        return Result.success(success);
    }

    @PostMapping("/warmup/{itemId}")
    @Operation(summary = "预热库存")
    public Result<Void> warmUp(@PathVariable Long itemId) {
        flashSaleService.warmUpStock(itemId);
        return Result.success();
    }
}
