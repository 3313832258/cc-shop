package com.ccshop.trade.feign;

import com.ccshop.common.core.Result;
import com.ccshop.trade.dto.StockChangeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "cc-product", path = "/product")
public interface ProductClient {

    @GetMapping("/sku/{skuId}")
    Result<SkuVO> getSku(@PathVariable("skuId") Long skuId);

    @PostMapping("/stock/decrease")
    Result<Void> decreaseStock(@RequestBody StockChangeRequest req);

    @PostMapping("/stock/increase")
    Result<Void> increaseStock(@RequestBody StockChangeRequest req);

    @lombok.Data
    class SkuVO {
        private Long id;
        private Long productId;
        private String productName;
        private String image;
        private Map<String, Object> specs;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
    }
}
