package com.ccshop.product.controller;

import com.ccshop.common.core.PageResult;
import com.ccshop.common.core.Result;
import com.ccshop.product.service.ProductService;
import com.ccshop.product.service.ProductService.ProductVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Tag(name = "商品")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/detail/{id}")
    public Result<ProductService.ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    @GetMapping("/sku/{skuId}")
    public Result<ProductService.SkuDetailVO> getSku(@PathVariable Long skuId) {
        return Result.success(productService.getSkuDetail(skuId));
    }

    @GetMapping("/list")
    public Result<PageResult<ProductVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String sort) {
        return Result.success(productService.list(page, size, categoryId, brandId, sort));
    }
}
