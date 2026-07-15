package com.ccshop.trade.service;

import cn.hutool.json.JSONUtil;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.Constants;
import com.ccshop.common.core.Result;
import com.ccshop.trade.dto.CartItemRequest;
import com.ccshop.trade.dto.CartItemVO;
import com.ccshop.trade.feign.ProductClient;
import com.ccshop.trade.feign.ProductClient.SkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductClient productClient;

    private String cartKey(Long userId) {
        return Constants.CART_PREFIX + userId;
    }

    public void addItem(Long userId, CartItemRequest req) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 通过 Feign 验证 SKU 是否存在并获取信息
        Result<SkuVO> result = productClient.getSku(req.getSkuId());
        if (result == null || result.getData() == null) {
            throw new BusinessException(400, "SKU不存在");
        }
        SkuVO sku = result.getData();
        if (sku.getStock() <= 0) {
            throw new BusinessException(400, "商品已售罄");
        }

        String key = cartKey(userId);
        String field = String.valueOf(req.getSkuId());
        String existing = (String) redisTemplate.opsForHash().get(key, field);

        if (existing != null) {
            CartItemVO vo = JSONUtil.toBean(existing, CartItemVO.class);
            int newQty = vo.getQuantity() + req.getQuantity();
            if (newQty > sku.getStock()) {
                newQty = sku.getStock();
            }
            vo.setQuantity(newQty);
            redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(vo));
        } else {
            CartItemVO vo = new CartItemVO();
            vo.setSkuId(sku.getId());
            vo.setProductId(sku.getProductId());
            vo.setProductName(sku.getProductName());
            vo.setProductImage(sku.getImage());
            vo.setSpecs(sku.getSpecs());
            vo.setPrice(sku.getPrice());
            vo.setOriginalPrice(sku.getOriginalPrice());
            vo.setQuantity(req.getQuantity());
            vo.setSelected(true);
            redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(vo));
        }
    }

    public void updateQuantity(Long userId, CartItemRequest req) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        String key = cartKey(userId);
        String field = String.valueOf(req.getSkuId());
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing == null) {
            throw new BusinessException(400, "购物车中不存在该商品");
        }
        CartItemVO vo = JSONUtil.toBean(existing, CartItemVO.class);
        vo.setQuantity(req.getQuantity());
        redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(vo));
    }

    public void removeItem(Long userId, Long skuId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        redisTemplate.opsForHash().delete(cartKey(userId), String.valueOf(skuId));
    }

    public List<CartItemVO> getList(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(userId));
        if (entries.isEmpty()) {
            return new ArrayList<>();
        }
        List<CartItemVO> list = new ArrayList<>();
        for (Object v : entries.values()) {
            list.add(JSONUtil.toBean((String) v, CartItemVO.class));
        }
        return list;
    }

    public void toggleSelect(Long userId, CartItemRequest req) {
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        String key = cartKey(userId);
        String field = String.valueOf(req.getSkuId());
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing == null) {
            throw new BusinessException(400, "购物车中不存在该商品");
        }
        CartItemVO vo = JSONUtil.toBean(existing, CartItemVO.class);
        vo.setSelected(req.getSelected() != null ? req.getSelected() : true);
        redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(vo));
    }
}
