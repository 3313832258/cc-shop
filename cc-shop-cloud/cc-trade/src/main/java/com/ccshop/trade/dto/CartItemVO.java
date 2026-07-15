package com.ccshop.trade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CartItemVO {
    private Long skuId;
    private Long productId;
    private String productName;
    private String productImage;
    /** SKU spec key-value pairs, e.g. {"颜色":"黑色","存储":"128GB"} */
    private Map<String, Object> specs;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer quantity;
    private Boolean selected;
}
