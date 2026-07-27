package com.ccshop.ai.feign;

import com.ccshop.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务 Feign 客户端
 */
@FeignClient(name = "cc-product", path = "/product")
public interface ProductClient {

    @GetMapping("/list")
    Result<ProductListResult> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size);

    @GetMapping("/search")
    Result<List<ProductItem>> search(@RequestParam String keyword);

    /** 商品列表返回结构 */
    @lombok.Data
    class ProductListResult {
        private List<ProductItem> records;
        private Long total;
    }

    /** 商品简要信息 */
    @lombok.Data
    class ProductItem {
        private Long id;
        private String name;
        private String description;
        private String image;
        private BigDecimal price;
        private Integer stock;
    }
}
