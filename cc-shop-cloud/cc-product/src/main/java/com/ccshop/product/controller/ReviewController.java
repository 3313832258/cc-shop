package com.ccshop.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.PageResult;
import com.ccshop.common.core.Result;
import com.ccshop.product.entity.ProductReview;
import com.ccshop.product.mapper.ProductReviewMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/product/review")
@RequiredArgsConstructor
@Tag(name = "商品评价")
public class ReviewController {

    private final ProductReviewMapper reviewMapper;

    @GetMapping
    @Operation(summary = "评价列表")
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

    @PostMapping
    @Operation(summary = "提交评价")
    public Result<Void> create(@RequestBody Map<String, Object> req) {
        ProductReview review = new ProductReview();
        review.setUserId(toLong(req.get("userId")));
        review.setProductId(toLong(req.get("productId")));
        review.setSkuId(toLong(req.get("skuId")));
        review.setOrderId(toLong(req.get("orderId")));
        review.setRating(toInteger(req.get("rating")));
        review.setContent((String) req.get("content"));
        review.setImages((String) req.get("images"));
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        return Result.success();
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }
}
