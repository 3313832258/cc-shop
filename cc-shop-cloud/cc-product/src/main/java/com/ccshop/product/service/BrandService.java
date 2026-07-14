package com.ccshop.product.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.Constants;
import com.ccshop.product.entity.Brand;
import com.ccshop.product.mapper.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandMapper brandMapper;
    private final StringRedisTemplate redisTemplate;

    private static final long CACHE_TTL_MINUTES = 60;

    public List<Brand> list() {
        String cached = redisTemplate.opsForValue().get(Constants.CACHE_BRAND_LIST);
        if (cached != null) {
            return JSONUtil.toList(JSONUtil.parseArray(cached), Brand.class);
        }

        List<Brand> list = brandMapper.selectList(
                new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getName));

        redisTemplate.opsForValue().set(
                Constants.CACHE_BRAND_LIST,
                JSONUtil.toJsonStr(list),
                CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return list;
    }

    public Brand getById(Long id) {
        return brandMapper.selectById(id);
    }
}
