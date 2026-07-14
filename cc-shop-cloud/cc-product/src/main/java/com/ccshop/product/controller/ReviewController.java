package com.ccshop.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.PageResult;
import com.ccshop.common.core.Result;
import com.ccshop.product.entity.ProductReview;
import com.ccshop.product.mapper.ProductReviewMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/review")
@RequiredArgsConstructor
@Tag(name = "商品评价（只读）")
public class ReviewController {

    private final ProductReviewMapper reviewMapper;

    @GetMapping
    public Result<PageResult<ProductReview>> list(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<ProductReview> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductReview::getProductId, productId)
          .orderByDesc(ProductReview::getCreatedAt);
        var p = reviewMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), qw);
        return Result.success(new PageResult<>(p.getRecords(), p.getTotal()));
    }
}
