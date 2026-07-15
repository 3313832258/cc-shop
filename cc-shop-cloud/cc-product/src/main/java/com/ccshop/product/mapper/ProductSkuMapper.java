package com.ccshop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccshop.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * 条件更新库存（防超卖）
     * @return 更新行数，0 表示库存不足
     */
    @Update("UPDATE product_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity}")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    /**
     * 回滚库存（无条件加回）
     */
    @Update("UPDATE product_sku SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int increaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
