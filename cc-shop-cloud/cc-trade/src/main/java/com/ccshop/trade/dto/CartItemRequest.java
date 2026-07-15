package com.ccshop.trade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 仅用于 select 操作 */
    private Boolean selected;
}
