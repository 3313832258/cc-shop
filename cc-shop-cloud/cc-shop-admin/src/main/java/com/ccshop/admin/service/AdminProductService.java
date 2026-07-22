package com.ccshop.admin.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ccshop.admin.entity.Product;
import com.ccshop.admin.entity.ProductSku;
import com.ccshop.admin.mapper.ProductMapper;
import com.ccshop.admin.mapper.ProductSkuMapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /**
     * 商品列表（分页）
     */
    public Map<String, Object> listProducts(int page, int size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 商家只能看自己的商品，管理员看所有
        Integer role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        if (role != null && role < 2 && userId != null) {
            wrapper.eq(Product::getMerchantId, userId);
        }
        wrapper.like(keyword != null && !keyword.isEmpty(), Product::getName, keyword)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getId);

        Page<Product> pageResult = productMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();

        for (Product product : pageResult.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("description", product.getDescription());
            item.put("brand_id", product.getBrandId());
            item.put("category_id", product.getCategoryId());
            item.put("merchant_id", product.getMerchantId());
            item.put("images", product.getImages());
            item.put("status", product.getStatus());
            item.put("created_at", product.getCreatedAt());
            item.put("updated_at", product.getUpdatedAt());
            list.add(item);
        }

        return Map.of("total", pageResult.getTotal(), "list", list);
    }

    /**
     * 商品详情
     */
    public Map<String, Object> getProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        checkOwnership(product);

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("brand_id", product.getBrandId());
        result.put("category_id", product.getCategoryId());
        result.put("merchant_id", product.getMerchantId());
        result.put("images", product.getImages());
        result.put("status", product.getStatus());
        result.put("created_at", product.getCreatedAt());
        result.put("updated_at", product.getUpdatedAt());

        // 查询 SKU
        LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(ProductSku::getProductId, id);
        List<ProductSku> skus = productSkuMapper.selectList(skuWrapper);
        result.put("skus", skus);

        return result;
    }

    /**
     * 新增商品（使用原生 JDBC 处理 JSON 列）
     */
    @Transactional
    public Long createProduct(Map<String, Object> req) {
        try (Connection conn = dataSource.getConnection()) {
            // 设置连接字符集
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET NAMES utf8mb4");
            }

            // 插入商品
            Long merchantId = UserContext.getUserId();
            String sql = "INSERT INTO product (name, description, brand_id, category_id, merchant_id, images, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, (String) req.get("name"));
                ps.setString(2, (String) req.get("description"));
                ps.setLong(3, toLong(req.get("brandId")));
                ps.setLong(4, toLong(req.get("categoryId")));
                ps.setLong(5, merchantId);
                ps.setString(6, toJsonString(req.get("images")));
                ps.setInt(7, req.get("status") != null ? (Integer) req.get("status") : 0);
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long productId = rs.getLong(1);

                        // 保存 SKU
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> skus = (List<Map<String, Object>>) req.get("skus");
                        if (skus != null && !skus.isEmpty()) {
                            String skuSql = "INSERT INTO product_sku (product_id, sku_code, price, original_price, stock, specs, image, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement skuPs = conn.prepareStatement(skuSql)) {
                                for (Map<String, Object> skuData : skus) {
                                    skuPs.setLong(1, productId);
                                    skuPs.setString(2, (String) skuData.get("skuCode"));
                                    skuPs.setBigDecimal(3, toBigDecimal(skuData.get("price")));
                                    skuPs.setBigDecimal(4, toBigDecimal(skuData.get("originalPrice")));
                                    skuPs.setInt(5, toInteger(skuData.get("stock")));
                                    skuPs.setString(6, toJsonString(skuData.get("specs")));
                                    skuPs.setString(7, (String) skuData.get("image"));
                                    skuPs.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                                    skuPs.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                                    skuPs.addBatch();
                                }
                                skuPs.executeBatch();
                            }
                        }

                        return productId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new BusinessException(500, "数据库错误: " + e.getMessage());
        }
        throw new BusinessException(500, "创建商品失败");
    }

    /**
     * 编辑商品（使用原生 JDBC 处理 JSON 列）
     */
    @Transactional
    public void updateProduct(Long id, Map<String, Object> req) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        checkOwnership(product);

        try (Connection conn = dataSource.getConnection()) {
            // 设置连接字符集
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET NAMES utf8mb4");
            }

            // 更新商品
            String sql = "UPDATE product SET name=?, description=?, brand_id=?, category_id=?, images=?, updated_at=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, (String) req.get("name"));
                ps.setString(2, (String) req.get("description"));
                ps.setLong(3, toLong(req.get("brandId")));
                ps.setLong(4, toLong(req.get("categoryId")));
                ps.setString(5, toJsonString(req.get("images")));
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.setLong(7, id);
                ps.executeUpdate();
            }

            // 删除旧 SKU
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM product_sku WHERE product_id = ?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            // 插入新 SKU
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> skus = (List<Map<String, Object>>) req.get("skus");
            if (skus != null && !skus.isEmpty()) {
                String skuSql = "INSERT INTO product_sku (product_id, sku_code, price, original_price, stock, specs, image, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement skuPs = conn.prepareStatement(skuSql)) {
                    for (Map<String, Object> skuData : skus) {
                        skuPs.setLong(1, id);
                        skuPs.setString(2, (String) skuData.get("skuCode"));
                        skuPs.setBigDecimal(3, toBigDecimal(skuData.get("price")));
                        skuPs.setBigDecimal(4, toBigDecimal(skuData.get("originalPrice")));
                        skuPs.setInt(5, toInteger(skuData.get("stock")));
                        skuPs.setString(6, toJsonString(skuData.get("specs")));
                        skuPs.setString(7, (String) skuData.get("image"));
                        skuPs.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                        skuPs.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                        skuPs.addBatch();
                    }
                    skuPs.executeBatch();
                }
            }
        } catch (SQLException e) {
            throw new BusinessException(500, "数据库错误: " + e.getMessage());
        }
    }

    /**
     * 上下架
     */
    public void updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        checkOwnership(product);
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    /**
     * 检查商品归属权限（商家只能操作自己的商品）
     */
    private void checkOwnership(Product product) {
        Integer role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        if (role != null && role < 2 && userId != null) {
            if (product.getMerchantId() == null || !product.getMerchantId().equals(userId)) {
                throw new BusinessException(403, "无权操作此商品");
            }
        }
    }

    /**
     * 获取分类列表
     */
    public List<Map<String, Object>> getCategories() {
        return jdbcTemplate.queryForList("SELECT id, name, parent_id FROM category ORDER BY sort_order");
    }

    /**
     * 获取品牌列表
     */
    public List<Map<String, Object>> getBrands() {
        return jdbcTemplate.queryForList("SELECT id, name FROM brand ORDER BY id");
    }

    private String toJsonString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        return JSONUtil.toJsonStr(obj);
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return new BigDecimal(obj.toString());
        return new BigDecimal(obj.toString());
    }
}
