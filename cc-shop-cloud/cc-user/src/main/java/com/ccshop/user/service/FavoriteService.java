package com.ccshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.UserContext;
import com.ccshop.user.entity.Favorite;
import com.ccshop.user.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    public List<Long> listProductIds() {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, UserContext.getUserId())
          .orderByDesc(Favorite::getCreatedAt);
        return favoriteMapper.selectList(qw).stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
    }

    public void add(Long productId) {
        Favorite f = new Favorite();
        f.setUserId(UserContext.getUserId());
        f.setProductId(productId);
        // 若已存在则忽略
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, UserContext.getUserId())
          .eq(Favorite::getProductId, productId);
        if (favoriteMapper.selectCount(qw) == 0) {
            favoriteMapper.insert(f);
        }
    }

    public void remove(Long productId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, UserContext.getUserId())
          .eq(Favorite::getProductId, productId);
        favoriteMapper.delete(qw);
    }

    public boolean isFavorite(Long productId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, UserContext.getUserId())
          .eq(Favorite::getProductId, productId);
        return favoriteMapper.selectCount(qw) > 0;
    }
}
