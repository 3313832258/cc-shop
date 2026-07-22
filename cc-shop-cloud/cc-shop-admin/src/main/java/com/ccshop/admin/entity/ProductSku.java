package com.ccshop.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_sku")
public class ProductSku {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String specs; // JSON 字符串
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String skuCode;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
