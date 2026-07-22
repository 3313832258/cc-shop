package com.ccshop.product.controller;

import com.ccshop.common.core.Result;
import com.ccshop.product.service.ProductService;
import com.ccshop.product.service.ProductService.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product/search")
@RequiredArgsConstructor
@Tag(name = "商品搜索")
public class SearchController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "商品搜索")
    public Result<List<ProductVO>> search(@RequestParam String keyword) {
        return Result.success(productService.search(keyword));
    }
}
