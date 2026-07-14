package com.ccshop.product.controller;

import com.ccshop.common.core.Result;
import com.ccshop.product.service.CategoryService;
import com.ccshop.product.service.CategoryService.CategoryTreeVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product/category")
@RequiredArgsConstructor
@Tag(name = "商品分类")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @GetMapping
    public Result<List<CategoryService.CategoryTreeVO>> list() {
        return Result.success(categoryService.tree());
    }
}
