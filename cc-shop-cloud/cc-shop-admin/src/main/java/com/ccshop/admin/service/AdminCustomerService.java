package com.ccshop.admin.service;

import com.ccshop.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 客户列表（分页）
     */
    public Map<String, Object> listCustomers(int page, int size, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT id, username, email, phone, avatar, created_at FROM user WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (username LIKE ? OR phone LIKE ? OR email LIKE ?)");
            countSql.append(" AND (username LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add((page - 1) * size);

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), queryParams.toArray());

        // 为每个用户附加订单统计
        for (Map<String, Object> user : list) {
            Long userId = ((Number) user.get("id")).longValue();
            attachOrderStats(user, userId);
        }

        return Map.of("total", total != null ? total : 0L, "list", list);
    }

    /**
     * 客户详情（含最近订单）
     */
    public Map<String, Object> getCustomerDetail(Long userId) {
        String sql = "SELECT id, username, email, phone, avatar, created_at FROM user WHERE id = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        Map<String, Object> user = list.get(0);
        attachOrderStats(user, userId);

        // 最近 10 笔订单
        String orderSql = "SELECT id, order_no, final_amount, status, created_at FROM `order` WHERE user_id = ? ORDER BY id DESC LIMIT 10";
        List<Map<String, Object>> orders = jdbcTemplate.queryForList(orderSql, userId);
        user.put("recentOrders", orders);

        // 收货地址
        String addrSql = "SELECT receiver_name, phone, province, city, district, detail, is_default FROM address WHERE user_id = ? ORDER BY is_default DESC";
        List<Map<String, Object>> addresses = jdbcTemplate.queryForList(addrSql, userId);
        user.put("addresses", addresses);

        return user;
    }

    private void attachOrderStats(Map<String, Object> user, Long userId) {
        Long orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `order` WHERE user_id = ?", Long.class, userId);
        Object totalSpent = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(final_amount), 0) FROM `order` WHERE user_id = ? AND status IN (1, 2, 3)",
                Object.class, userId);

        user.put("orderCount", orderCount != null ? orderCount : 0L);
        user.put("totalSpent", totalSpent != null ? totalSpent.toString() : "0");
    }
}
