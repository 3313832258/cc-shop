package com.ccshop.admin.service;

import com.ccshop.common.core.BusinessException;
import com.ccshop.common.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 订单列表（分页）
     */
    public Map<String, Object> listOrders(int page, int size, Integer status, String keyword, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT o.* FROM `order` o WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM `order` o WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND o.status = ?");
            countSql.append(" AND o.status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (o.order_no LIKE ? OR o.address_snapshot LIKE ?)");
            countSql.append(" AND (o.order_no LIKE ? OR o.address_snapshot LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND o.created_at >= ?");
            countSql.append(" AND o.created_at >= ?");
            params.add(startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND o.created_at <= ?");
            countSql.append(" AND o.created_at <= ?");
            params.add(endDate + " 23:59:59");
        }

        sql.append(" ORDER BY o.id DESC LIMIT ? OFFSET ?");
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add((page - 1) * size);

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), queryParams.toArray());

        // 为每个订单添加状态描述
        for (Map<String, Object> order : list) {
            Integer statusVal = (Integer) order.get("status");
            order.put("statusDesc", OrderStatus.fromCode(statusVal).getDesc());
        }

        return Map.of("total", total != null ? total : 0L, "list", list);
    }

    /**
     * 订单详情
     */
    public Map<String, Object> getOrder(Long id) {
        String sql = "SELECT * FROM `order` WHERE id = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
        if (list.isEmpty()) {
            throw new BusinessException(404, "订单不存在");
        }

        Map<String, Object> order = list.get(0);
        Integer statusVal = (Integer) order.get("status");
        order.put("statusDesc", OrderStatus.fromCode(statusVal).getDesc());

        // 查询订单项
        String itemSql = "SELECT * FROM order_item WHERE order_id = ?";
        List<Map<String, Object>> items = jdbcTemplate.queryForList(itemSql, id);
        order.put("items", items);

        return order;
    }

    /**
     * 发货
     */
    public void shipOrder(Long id) {
        String sql = "UPDATE `order` SET status = ? WHERE id = ? AND status = ?";
        int rows = jdbcTemplate.update(sql, OrderStatus.SHIPPED.getCode(), id, OrderStatus.PENDING_SHIPMENT.getCode());
        if (rows == 0) {
            throw new BusinessException(400, "订单状态不正确，无法发货");
        }
    }
}
