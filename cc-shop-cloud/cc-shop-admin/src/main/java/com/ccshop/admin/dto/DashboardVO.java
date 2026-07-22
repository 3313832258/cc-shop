package com.ccshop.admin.dto;

import lombok.Data;

@Data
public class DashboardVO {
    private Long totalOrders;
    private Long todayOrders;
    private Long totalUsers;
    private Long totalProducts;
    private java.math.BigDecimal totalRevenue;
    private java.math.BigDecimal todayRevenue;
    private Long pendingOrders;      // 待付款
    private Long pendingShipments;   // 待发货
    private Long pendingAftersales;

    /** 7 天趋势 */
    private java.util.List<TrendPoint> trend;
    /** 低库存预警（库存 < 10） */
    private java.util.List<LowStockItem> lowStockItems;

    @Data
    public static class TrendPoint {
        private String date;        // yyyy-MM-dd
        private Long orderCount;
        private java.math.BigDecimal revenue;
    }

    @Data
    public static class LowStockItem {
        private Long productId;
        private Long skuId;
        private String productName;
        private String skuCode;
        private Integer stock;
    }
}
