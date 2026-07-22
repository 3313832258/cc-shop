package com.ccshop.admin.service;

import com.ccshop.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private final JdbcTemplate jdbcTemplate;

    private static final Map<Integer, String> TYPE_MAP = Map.of(
            0, "满减券",
            1, "折扣券"
    );

    /**
     * 优惠券列表
     */
    public Map<String, Object> listCoupons(int page, int size) {
        String countSql = "SELECT COUNT(*) FROM coupon";
        String sql = "SELECT * FROM coupon ORDER BY id DESC LIMIT ? OFFSET ?";

        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, size, (page - 1) * size);

        // 添加类型描述
        for (Map<String, Object> item : list) {
            Integer type = (Integer) item.get("type");
            item.put("typeDesc", TYPE_MAP.getOrDefault(type, "未知"));
        }

        return Map.of("total", total != null ? total : 0L, "list", list);
    }

    /**
     * 优惠券详情
     */
    public Map<String, Object> getCoupon(Long id) {
        String sql = "SELECT * FROM coupon WHERE id = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
        if (list.isEmpty()) {
            throw new BusinessException(404, "优惠券不存在");
        }

        Map<String, Object> item = list.get(0);
        Integer type = (Integer) item.get("type");
        item.put("typeDesc", TYPE_MAP.getOrDefault(type, "未知"));

        return item;
    }

    /**
     * 新增优惠券
     */
    public Long createCoupon(Map<String, Object> req) {
        String sql = "INSERT INTO coupon (name, type, value, min_order_amount, total_count, remaining_count, start_time, end_time, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Integer totalCount = toInteger(req.get("totalCount"));
        jdbcTemplate.update(sql,
                req.get("name"),
                req.get("type"),
                req.get("value"),
                req.get("minOrderAmount"),
                totalCount,
                req.get("remainingCount") != null ? req.get("remainingCount") : totalCount,
                req.get("startTime"),
                req.get("endTime"),
                LocalDateTime.now());

        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id;
    }

    /**
     * 优惠券领取/使用明细
     */
    public Map<String, Object> getCouponClaims(Long couponId, int page, int size, Integer status) {
        StringBuilder sql = new StringBuilder(
                "SELECT uc.id, uc.user_id, uc.status, uc.used_order_id, uc.created_at, u.username " +
                "FROM user_coupon uc LEFT JOIN user u ON uc.user_id = u.id WHERE uc.coupon_id = ?");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM user_coupon uc WHERE uc.coupon_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(couponId);

        if (status != null) {
            sql.append(" AND uc.status = ?");
            countSql.append(" AND uc.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY uc.id DESC LIMIT ? OFFSET ?");
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add((page - 1) * size);

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), queryParams.toArray());

        Map<Integer, String> statusDesc = Map.of(0, "可用", 1, "已使用", 2, "已过期");
        for (Map<String, Object> item : list) {
            Integer s = (Integer) item.get("status");
            item.put("statusDesc", statusDesc.getOrDefault(s, "未知"));
        }

        // 全量统计（不受 status 筛选影响）
        Long totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_coupon WHERE coupon_id = ?", Long.class, couponId);
        Long usedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_coupon WHERE coupon_id = ? AND status = 1", Long.class, couponId);

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", totalCount != null ? totalCount : 0L);
        stats.put("used", usedCount != null ? usedCount : 0L);
        stats.put("unused", (totalCount != null ? totalCount : 0L) - (usedCount != null ? usedCount : 0L));

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", total != null ? total : 0L);
        result.put("list", list);
        result.put("stats", stats);
        return result;
    }

    /**
     * 编辑优惠券
     */
    public void updateCoupon(Long id, Map<String, Object> req) {
        String sql = "UPDATE coupon SET name=?, type=?, value=?, min_order_amount=?, start_time=?, end_time=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                req.get("name"),
                req.get("type"),
                req.get("value"),
                req.get("minOrderAmount"),
                req.get("startTime"),
                req.get("endTime"),
                id);
        if (rows == 0) {
            throw new BusinessException(404, "优惠券不存在");
        }
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }
}
