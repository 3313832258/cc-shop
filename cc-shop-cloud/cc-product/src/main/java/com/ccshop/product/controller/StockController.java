package com.ccshop.product.controller;

import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.Result;
import com.ccshop.product.dto.StockChangeRequest;
import com.ccshop.product.mapper.ProductSkuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/stock")
@RequiredArgsConstructor
@Tag(name = "库存")
public class StockController {

    private final ProductSkuMapper skuMapper;

    @PostMapping("/decrease")
    @Operation(summary = "批量扣减库存（条件更新防超卖）")
    @Transactional
    public Result<Void> decrease(@RequestBody StockChangeRequest req) {
        for (StockChangeRequest.StockItem item : req.getItems()) {
            int updated = skuMapper.decreaseStock(item.getSkuId(), item.getQuantity());
            if (updated == 0) {
                throw new BusinessException(400, "库存不足: skuId=" + item.getSkuId());
            }
        }
        return Result.success();
    }

    @PostMapping("/increase")
    @Operation(summary = "批量回滚库存")
    @Transactional
    public Result<Void> increase(@RequestBody StockChangeRequest req) {
        for (StockChangeRequest.StockItem item : req.getItems()) {
            skuMapper.increaseStock(item.getSkuId(), item.getQuantity());
        }
        return Result.success();
    }
}
