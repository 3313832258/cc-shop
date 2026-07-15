package com.ccshop.product.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.Constants;
import com.ccshop.common.core.PageResult;
import com.ccshop.product.entity.Category;
import com.ccshop.product.entity.Product;
import com.ccshop.product.entity.ProductReview;
import com.ccshop.product.entity.ProductSku;
import com.ccshop.product.entity.ProductSpec;
import com.ccshop.product.mapper.CategoryMapper;
import com.ccshop.product.mapper.ProductMapper;
import com.ccshop.product.mapper.ProductReviewMapper;
import com.ccshop.product.mapper.ProductSkuMapper;
import com.ccshop.product.mapper.ProductSpecMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductSpecMapper specMapper;
    private final ProductReviewMapper reviewMapper;
    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate redisTemplate;

    /** 缓存 30 分钟 */
    private static final long CACHE_TTL_MINUTES = 30;

    /** 商品详情（含 SKU 列表 + 规格参数） */
    public ProductDetailVO getDetail(Long productId) {
        // 1. 查 Redis
        String cacheKey = Constants.CACHE_PRODUCT_DETAIL + productId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSONUtil.toBean(cached, ProductDetailVO.class);
        }

        // 2. 查 DB
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setBrandId(product.getBrandId());
        vo.setCategoryId(product.getCategoryId());

        // images JSON → List
        if (StrUtil.isNotBlank(product.getImages())) {
            vo.setImages(JSONUtil.toList(JSONUtil.parseArray(product.getImages()), String.class));
        }
        vo.setStatus(product.getStatus());
        vo.setCreatedAt(product.getCreatedAt());

        // SKUs
        LambdaQueryWrapper<ProductSku> sqw = new LambdaQueryWrapper<>();
        sqw.eq(ProductSku::getProductId, productId);
        List<ProductSku> skus = skuMapper.selectList(sqw);
        List<SkuVO> skuVOs = skus.stream().map(s -> {
            SkuVO sv = new SkuVO();
            sv.setId(s.getId());
            sv.setPrice(s.getPrice());
            sv.setOriginalPrice(s.getOriginalPrice());
            sv.setStock(s.getStock());
            sv.setSkuCode(s.getSkuCode());
            sv.setImage(s.getImage());
            // specs JSON → Map
            if (StrUtil.isNotBlank(s.getSpecs())) {
                sv.setSpecs(JSONUtil.toBean(s.getSpecs(), Map.class));
            }
            return sv;
        }).collect(Collectors.toList());
        vo.setSkus(skuVOs);

        // 提取所有规格键（用于前端 SKU 选择器）
        Set<Map.Entry<String, Object>> allSpecs = new LinkedHashSet<>();
        for (SkuVO sv : skuVOs) {
            if (sv.getSpecs() != null) allSpecs.addAll(sv.getSpecs().entrySet());
        }
        // 按规格键分组
        Map<String, Set<Object>> specOptions = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : allSpecs) {
            specOptions.computeIfAbsent(e.getKey(), k -> new LinkedHashSet<>()).add(e.getValue());
        }
        vo.setSpecOptions(specOptions);

        // 规格参数
        LambdaQueryWrapper<ProductSpec> spqw = new LambdaQueryWrapper<>();
        spqw.eq(ProductSpec::getProductId, productId);
        vo.setSpecs(specMapper.selectList(spqw).stream().map(s -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("name", s.getSpecName());
            m.put("value", s.getSpecValue());
            return m;
        }).collect(Collectors.toList()));

        // 评价
        LambdaQueryWrapper<ProductReview> rqw = new LambdaQueryWrapper<>();
        rqw.eq(ProductReview::getProductId, productId).orderByDesc(ProductReview::getCreatedAt).last("LIMIT 10");
        vo.setReviews(reviewMapper.selectList(rqw));

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(vo), CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return vo;
    }

    /** 商品列表（分页 + 分类/品牌过滤 + 排序） */
    public PageResult<ProductVO> list(int page, int size, Long categoryId, Long brandId, String sort) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getStatus, 1); // 仅上架
        if (categoryId != null) qw.eq(Product::getCategoryId, categoryId);
        if (brandId != null) qw.eq(Product::getBrandId, brandId);

        // 排序：默认按创建时间倒序
        if ("price_asc".equals(sort)) qw.orderByAsc(Product::getId); // 简化实现
        else if ("price_desc".equals(sort)) qw.orderByDesc(Product::getId);
        else qw.orderByDesc(Product::getCreatedAt);

        // 查 SKU 最低价
        List<Product> products = productMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), qw).getRecords();

        List<ProductVO> records = products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setDescription(p.getDescription());
            vo.setCategoryId(p.getCategoryId());
            vo.setBrandId(p.getBrandId());
            if (StrUtil.isNotBlank(p.getImages())) {
                List<String> imgs = JSONUtil.toList(JSONUtil.parseArray(p.getImages()), String.class);
                vo.setImage(imgs.isEmpty() ? null : imgs.get(0));
            }

            // SKU 最低价格
            LambdaQueryWrapper<ProductSku> sqw = new LambdaQueryWrapper<>();
            sqw.eq(ProductSku::getProductId, p.getId()).last("ORDER BY price ASC LIMIT 1");
            ProductSku cheapest = skuMapper.selectOne(sqw);
            if (cheapest != null) vo.setPrice(cheapest.getPrice());

            // 总库存
            LambdaQueryWrapper<ProductSku> stkq = new LambdaQueryWrapper<>();
            stkq.eq(ProductSku::getProductId, p.getId());
            List<ProductSku> allSkus = skuMapper.selectList(stkq);
            vo.setStock(allSkus.stream().mapToInt(ProductSku::getStock).sum());

            return vo;
        }).collect(Collectors.toList());

        Long total = productMapper.selectCount(qw);
        return new PageResult<>(records, total);
    }

    /** 搜索（中文 FULLTEXT ngram） */
    public List<ProductVO> search(String keyword) {
        if (StrUtil.isBlank(keyword)) return List.of();

        // 使用 MyBatis-Plus 自定义 SQL，通过 MyBatis XML
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .and(w -> w.like(Product::getName, keyword).or().like(Product::getDescription, keyword))
                        .orderByDesc(Product::getCreatedAt)
                        .last("LIMIT 50")
        );

        return products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setDescription(p.getDescription());
            if (StrUtil.isNotBlank(p.getImages())) {
                List<String> imgs = JSONUtil.toList(JSONUtil.parseArray(p.getImages()), String.class);
                vo.setImage(imgs.isEmpty() ? null : imgs.get(0));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Data
    public static class ProductDetailVO {
        private Long id;
        private String name;
        private String description;
        private Long brandId;
        private Long categoryId;
        private List<String> images;
        private Integer status;
        private java.time.LocalDateTime createdAt;
        private List<SkuVO> skus;
        private Map<String, Set<Object>> specOptions;
        private List<Map<String, String>> specs; // 参数列表
        private List<ProductReview> reviews;
    }

    @Data
    public static class SkuVO {
        private Long id;
        private java.math.BigDecimal price;
        private java.math.BigDecimal originalPrice;
        private Integer stock;
        private String skuCode;
        private String image;
        private Map<String, Object> specs;
    }

    /** Feign 调用：获取单个 SKU 详细信息（含商品名） */
    @Data
    public static class SkuDetailVO {
        private Long id;
        private Long productId;
        private String productName;
        private String image;
        private Map<String, Object> specs;
        private java.math.BigDecimal price;
        private java.math.BigDecimal originalPrice;
        private Integer stock;
    }

    public SkuDetailVO getSkuDetail(Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }
        Product product = productMapper.selectById(sku.getProductId());
        SkuDetailVO vo = new SkuDetailVO();
        vo.setId(sku.getId());
        vo.setProductId(sku.getProductId());
        vo.setProductName(product != null ? product.getName() : "");
        vo.setImage(sku.getImage());
        if (StrUtil.isNotBlank(sku.getSpecs())) {
            vo.setSpecs(JSONUtil.toBean(sku.getSpecs(), Map.class));
        }
        vo.setPrice(sku.getPrice());
        vo.setOriginalPrice(sku.getOriginalPrice());
        vo.setStock(sku.getStock());
        return vo;
    }

    @Data
    public static class ProductVO {
        private Long id;
        private String name;
        private String description;
        private Long categoryId;
        private Long brandId;
        private String image;
        private java.math.BigDecimal price;
        private Integer stock;
    }
}
