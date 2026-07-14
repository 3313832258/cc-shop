package com.ccshop.product.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.Constants;
import com.ccshop.product.entity.Category;
import com.ccshop.product.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate redisTemplate;

    private static final long CACHE_TTL_MINUTES = 60;

    /** 树形分类（一级 → 二级） */
    public List<CategoryTreeVO> tree() {
        String cached = redisTemplate.opsForValue().get(Constants.CACHE_CATEGORY_TREE);
        if (cached != null) {
            return JSONUtil.toList(JSONUtil.parseArray(cached), CategoryTreeVO.class);
        }

        List<Category> all = categoryMapper.selectList(null);
        List<CategoryTreeVO> roots = all.stream()
                .filter(c -> c.getParentId() == 0)
                .sorted(Comparator.comparing(Category::getSortOrder))
                .map(c -> {
                    CategoryTreeVO vo = new CategoryTreeVO();
                    vo.setId(c.getId());
                    vo.setName(c.getName());
                    vo.setIcon(c.getIcon());
                    return vo;
                }).collect(Collectors.toList());

        for (CategoryTreeVO root : roots) {
            List<CategoryTreeVO> children = all.stream()
                    .filter(c -> c.getParentId() != null && c.getParentId().equals(root.getId()))
                    .sorted(Comparator.comparing(Category::getSortOrder))
                    .map(c -> {
                        CategoryTreeVO cv = new CategoryTreeVO();
                        cv.setId(c.getId());
                        cv.setName(c.getName());
                        return cv;
                    }).collect(Collectors.toList());
            root.setChildren(children);
        }

        redisTemplate.opsForValue().set(
                Constants.CACHE_CATEGORY_TREE,
                JSONUtil.toJsonStr(roots),
                CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return roots;
    }

    public List<Category> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    public static class CategoryTreeVO {
        private Long id;
        private String name;
        private String icon;
        private List<CategoryTreeVO> children;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public List<CategoryTreeVO> getChildren() { return children; }
        public void setChildren(List<CategoryTreeVO> children) { this.children = children; }
    }
}
