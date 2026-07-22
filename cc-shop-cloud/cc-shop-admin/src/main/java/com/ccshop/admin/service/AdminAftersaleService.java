package com.ccshop.admin.service;

import com.ccshop.common.core.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAftersaleService {

    private final JdbcTemplate jdbcTemplate;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            0, "待审核",
            1, "审核通过",
            2, "审核拒绝",
            3, "退款中",
            4, "退款成功",
            5, "已取消"
    );

    private static final Map<String, String> TYPE_MAP = Map.of(
            "refund", "仅退款",
            "return_refund", "退货退款"
    );

    /**
     * 售后列表
     */
    public Map<String, Object> listAftersales(int page, int size, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT a.*, o.order_no FROM aftersale_record a LEFT JOIN `order` o ON a.order_id = o.id WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM aftersale_record a WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND a.status = ?");
            countSql.append(" AND a.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY a.id DESC LIMIT ? OFFSET ?");
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add((page - 1) * size);

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), queryParams.toArray());

        // 添加状态和类型描述
        for (Map<String, Object> item : list) {
            Integer statusVal = (Integer) item.get("status");
            item.put("statusDesc", STATUS_MAP.getOrDefault(statusVal, "未知"));
            String type = (String) item.get("type");
            item.put("typeDesc", TYPE_MAP.getOrDefault(type, "未知"));
        }

        return Map.of("total", total != null ? total : 0L, "list", list);
    }

    /**
     * 售后详情
     */
    public Map<String, Object> getAftersale(Long id) {
        String sql = "SELECT a.*, o.order_no FROM aftersale_record a LEFT JOIN `order` o ON a.order_id = o.id WHERE a.id = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
        if (list.isEmpty()) {
            throw new BusinessException(404, "售后记录不存在");
        }

        Map<String, Object> item = list.get(0);
        Integer statusVal = (Integer) item.get("status");
        item.put("statusDesc", STATUS_MAP.getOrDefault(statusVal, "未知"));
        String type = (String) item.get("type");
        item.put("typeDesc", TYPE_MAP.getOrDefault(type, "未知"));

        return item;
    }

    /**
     * 审批通过
     */
    public void approve(Long id) {
        String sql = "UPDATE aftersale_record SET status = 1 WHERE id = ? AND status = 0";
        int rows = jdbcTemplate.update(sql, id);
        if (rows == 0) {
            throw new BusinessException(400, "售后状态不正确，无法审批");
        }
    }

    /**
     * 审批拒绝
     */
    public void reject(Long id) {
        String sql = "UPDATE aftersale_record SET status = 2 WHERE id = ? AND status = 0";
        int rows = jdbcTemplate.update(sql, id);
        if (rows == 0) {
            throw new BusinessException(400, "售后状态不正确，无法审批");
        }
    }
}
