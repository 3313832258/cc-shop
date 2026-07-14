package com.ccshop.product.controller;

import com.ccshop.common.core.Result;
import com.ccshop.product.entity.Brand;
import com.ccshop.product.service.BrandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product/brand")
@RequiredArgsConstructor
@Tag(name = "品牌")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public Result<List<Brand>> list() {
        return Result.success(brandService.list());
    }

    @GetMapping("/{id}")
    public Result<Brand> getById(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }
}
