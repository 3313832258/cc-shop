package com.ccshop.user.controller;

import com.ccshop.common.core.Result;
import com.ccshop.user.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/favorite")
@RequiredArgsConstructor
@Tag(name = "收藏")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Result<List<Long>> list() {
        return Result.success(favoriteService.listProductIds());
    }

    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId) {
        favoriteService.add(productId);
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        favoriteService.remove(productId);
        return Result.success();
    }

    @GetMapping("/{productId}/check")
    public Result<Boolean> isFavorite(@PathVariable Long productId) {
        return Result.success(favoriteService.isFavorite(productId));
    }
}
