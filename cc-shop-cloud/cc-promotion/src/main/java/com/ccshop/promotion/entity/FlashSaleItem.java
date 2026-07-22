package com.ccshop.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("flash_sale_item")
public class FlashSaleItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long productId;
    private Long skuId;
    private BigDecimal flashPrice;
    private Integer totalStock;
    private Integer availableStock;
    private Integer limitPerUser;
}
