package com.ccshop.admin.service;

import com.ccshop.admin.dto.DashboardVO;
import com.ccshop.admin.dto.DashboardVO.LowStockItem;
import com.ccshop.admin.dto.DashboardVO.TrendPoint;
import com.ccshop.common.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        // 总订单数
        vo.setTotalOrders(queryCount("SELECT COUNT(*) FROM `order`"));

        // 今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayOrders(queryCount("SELECT COUNT(*) FROM `order` WHERE created_at >= ?", todayStart));

        // 总用户数
        vo.setTotalUsers(queryCount("SELECT COUNT(*) FROM user"));

        // 总商品数
        vo.setTotalProducts(queryCount("SELECT COUNT(*) FROM product"));

        // 总销售额
        vo.setTotalRevenue(queryAmount("SELECT COALESCE(SUM(final_amount), 0) FROM `order` WHERE status IN (?, ?, ?)",
                OrderStatus.PENDING_SHIPMENT.getCode(), OrderStatus.SHIPPED.getCode(), OrderStatus.COMPLETED.getCode()));

        // 今日销售额
        vo.setTodayRevenue(queryAmount("SELECT COALESCE(SUM(final_amount), 0) FROM `order` WHERE status IN (?, ?, ?) AND created_at >= ?",
                OrderStatus.PENDING_SHIPMENT.getCode(), OrderStatus.SHIPPED.getCode(), OrderStatus.COMPLETED.getCode(), todayStart));

        // 待付款
        vo.setPendingOrders(queryCount("SELECT COUNT(*) FROM `order` WHERE status = ?", OrderStatus.PENDING_PAYMENT.getCode()));

        // 待发货
        vo.setPendingShipments(queryCount("SELECT COUNT(*) FROM `order` WHERE status = ?", OrderStatus.PENDING_SHIPMENT.getCode()));

        // 待处理售后
        vo.setPendingAftersales(queryCount("SELECT COUNT(*) FROM aftersale_record WHERE status = 0"));

        // 7 天趋势
        vo.setTrend(queryTrend());

        // 低库存预警
        vo.setLowStockItems(queryLowStock());

        return vo;
    }

    /** 最近 7 天每日订单数和销售额 */
    private List<TrendPoint> queryTrend() {
        List<TrendPoint> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(day.plusDays(1), LocalTime.MIN);

            Long count = queryCount(
                    "SELECT COUNT(*) FROM `order` WHERE created_at >= ? AND created_at < ?",
                    dayStart, dayEnd);

            BigDecimal revenue = queryAmount(
                    "SELECT COALESCE(SUM(final_amount), 0) FROM `order` WHERE status IN (?, ?, ?) AND created_at >= ? AND created_at < ?",
                    OrderStatus.PENDING_SHIPMENT.getCode(), OrderStatus.SHIPPED.getCode(), OrderStatus.COMPLETED.getCode(),
                    dayStart, dayEnd);

            TrendPoint point = new TrendPoint();
            point.setDate(day.format(fmt));
            point.setOrderCount(count);
            point.setRevenue(revenue);
            trend.add(point);
        }
        return trend;
    }

    /** SKU 库存 < 10 的商品 */
    private List<LowStockItem> queryLowStock() {
        String sql = """
                SELECT s.id AS sku_id, s.product_id, s.sku_code, s.stock, p.name AS product_name
                FROM product_sku s
                JOIN product p ON s.product_id = p.id
                WHERE s.stock < 10 AND s.stock >= 0
                ORDER BY s.stock ASC
                LIMIT 20
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<LowStockItem> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            LowStockItem item = new LowStockItem();
            item.setSkuId(((Number) row.get("sku_id")).longValue());
            item.setProductId(((Number) row.get("product_id")).longValue());
            item.setSkuCode((String) row.get("sku_code"));
            item.setStock(((Number) row.get("stock")).intValue());
            item.setProductName((String) row.get("product_name"));
            items.add(item);
        }
        return items;
    }

    private Long queryCount(String sql, Object... args) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, args);
        return result != null ? result : 0L;
    }

    private BigDecimal queryAmount(String sql, Object... args) {
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return result != null ? result : BigDecimal.ZERO;
    }
}
