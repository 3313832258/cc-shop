package com.ccshop.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.trade.dto.AftersaleApplyRequest;
import com.ccshop.trade.dto.AftersaleVO;
import com.ccshop.trade.entity.AftersaleRecord;
import com.ccshop.trade.entity.Order;
import com.ccshop.trade.entity.OrderItem;
import com.ccshop.trade.mapper.AftersaleRecordMapper;
import com.ccshop.trade.mapper.OrderItemMapper;
import com.ccshop.trade.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AftersaleService {

    private final AftersaleRecordMapper aftersaleRecordMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public void apply(Long userId, AftersaleApplyRequest req) {
        // 校验订单
        Order order = orderMapper.selectById(req.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }
        if (order.getStatus() < 1 || order.getStatus() > 3) {
            throw new BusinessException(400, "当前订单状态不可申请售后");
        }

        // 校验订单项
        OrderItem orderItem = orderItemMapper.selectById(req.getOrderItemId());
        if (orderItem == null || !orderItem.getOrderId().equals(order.getId())) {
            throw new BusinessException(400, "订单项不存在");
        }

        // 检查是否已申请
        Long count = aftersaleRecordMapper.selectCount(
                new LambdaQueryWrapper<AftersaleRecord>()
                        .eq(AftersaleRecord::getOrderItemId, req.getOrderItemId())
                        .in(AftersaleRecord::getStatus, 0, 1, 3));
        if (count > 0) {
            throw new BusinessException(400, "该商品已有售后申请在处理中");
        }

        // 创建售后记录
        AftersaleRecord record = new AftersaleRecord();
        record.setOrderId(order.getId());
        record.setOrderItemId(orderItem.getId());
        record.setUserId(userId);
        record.setType(req.getType());
        record.setReason(req.getReason());
        record.setStatus(0); // 待审核
        record.setAmount(orderItem.getPrice().multiply(new java.math.BigDecimal(orderItem.getQuantity())));
        aftersaleRecordMapper.insert(record);
    }

    public List<AftersaleVO> getAftersales(Long userId, Long orderId) {
        LambdaQueryWrapper<AftersaleRecord> qw = new LambdaQueryWrapper<AftersaleRecord>()
                .eq(AftersaleRecord::getUserId, userId)
                .orderByDesc(AftersaleRecord::getCreatedAt);
        if (orderId != null) {
            qw.eq(AftersaleRecord::getOrderId, orderId);
        }

        List<AftersaleRecord> records = aftersaleRecordMapper.selectList(qw);
        List<AftersaleVO> vos = new ArrayList<>();
        for (AftersaleRecord record : records) {
            vos.add(toVO(record));
        }
        return vos;
    }

    public AftersaleVO getAftersale(Long userId, Long id) {
        AftersaleRecord record = aftersaleRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(404, "售后记录不存在");
        }
        return toVO(record);
    }

    @Transactional
    public void cancel(Long userId, Long id) {
        AftersaleRecord record = aftersaleRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(404, "售后记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException(400, "只能取消待审核的售后申请");
        }
        record.setStatus(5); // 已取消
        aftersaleRecordMapper.updateById(record);
    }

    private AftersaleVO toVO(AftersaleRecord record) {
        AftersaleVO vo = new AftersaleVO();
        vo.setId(record.getId());
        vo.setOrderId(record.getOrderId());
        vo.setOrderItemId(record.getOrderItemId());
        vo.setType(record.getType());
        vo.setTypeText("refund".equals(record.getType()) ? "仅退款" : "退货退款");
        vo.setReason(record.getReason());
        vo.setStatus(record.getStatus());
        vo.setStatusText(getStatusText(record.getStatus()));
        vo.setAmount(record.getAmount());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "处理中";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}
