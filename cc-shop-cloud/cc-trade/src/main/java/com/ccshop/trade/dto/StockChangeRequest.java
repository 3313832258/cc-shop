package com.ccshop.trade.dto;

import lombok.Data;
import java.util.List;

@Data
public class StockChangeRequest {
    private List<StockItem> items;

    @Data
    public static class StockItem {
        private Long skuId;
        private Integer quantity;
    }
}
