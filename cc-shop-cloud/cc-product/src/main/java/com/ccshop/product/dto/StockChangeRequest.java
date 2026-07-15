package com.ccshop.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockChangeRequest {

    @NotNull
    private List<StockItem> items;

    @Data
    public static class StockItem {
        @NotNull
        private Long skuId;
        @Min(1)
        private Integer quantity;
    }
}
