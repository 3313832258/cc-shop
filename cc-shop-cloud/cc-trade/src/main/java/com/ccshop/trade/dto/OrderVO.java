package com.ccshop.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Integer status;
    private String statusDesc;
    private String addressSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private List<OrderItemVO> items;

    @Data
    public static class OrderItemVO {
        private Long id;
        private Long productId;
        private Long skuId;
        private String productName;
        private String skuSpecs;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
    }
}
