package com.ccshop.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.trade.entity.LogisticsRecord;
import com.ccshop.trade.entity.LogisticsStep;
import com.ccshop.trade.mapper.LogisticsRecordMapper;
import com.ccshop.trade.mapper.LogisticsStepMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流模拟器：定时推进物流状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsSimulator {

    private final LogisticsRecordMapper logisticsRecordMapper;
    private final LogisticsStepMapper logisticsStepMapper;

    /**
     * 创建初始物流记录（支付成功后调用）
     */
    public void createLogistics(Long orderId) {
        LogisticsRecord record = new LogisticsRecord();
        record.setOrderId(orderId);
        record.setCarrier("CC快递");
        record.setTrackingNo("CC" + System.currentTimeMillis());
        record.setStatus(1); // 已揽收
        record.setCurrentLocation("发货仓库");
        record.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
        logisticsRecordMapper.insert(record);

        // 添加第一个步骤
        LogisticsStep step = new LogisticsStep();
        step.setLogisticsId(record.getId());
        step.setDescription("商品已揽收");
        step.setLocation("发货仓库");
        step.setTimestamp(LocalDateTime.now());
        logisticsStepMapper.insert(step);

        log.info("创建物流记录: orderId={}, trackingNo={}", orderId, record.getTrackingNo());
    }

    /**
     * 每30秒推进一次物流状态（模拟）
     */
    @Scheduled(fixedRate = 30000)
    public void advanceLogistics() {
        // 查找所有未签收的物流记录
        List<LogisticsRecord> records = logisticsRecordMapper.selectList(
                new LambdaQueryWrapper<LogisticsRecord>()
                        .in(LogisticsRecord::getStatus, 1, 2, 3));

        for (LogisticsRecord record : records) {
            try {
                advanceOneStep(record);
            } catch (Exception e) {
                log.error("推进物流失败: orderId={}", record.getOrderId(), e);
            }
        }
    }

    private void advanceOneStep(LogisticsRecord record) {
        int nextStatus = record.getStatus() + 1;
        if (nextStatus > 4) return;

        String description;
        String location;

        switch (nextStatus) {
            case 2 -> {
                description = "商品已发出，运输中";
                location = "物流中心";
            }
            case 3 -> {
                description = "商品已到达目的地，派送中";
                location = "目的地城市";
            }
            case 4 -> {
                description = "商品已签收";
                location = "收货地址";
            }
            default -> {
                description = "状态更新";
                location = record.getCurrentLocation();
            }
        }

        // 更新物流记录
        record.setStatus(nextStatus);
        record.setCurrentLocation(location);
        logisticsRecordMapper.updateById(record);

        // 添加步骤
        LogisticsStep step = new LogisticsStep();
        step.setLogisticsId(record.getId());
        step.setDescription(description);
        step.setLocation(location);
        step.setTimestamp(LocalDateTime.now());
        logisticsStepMapper.insert(step);

        log.info("物流推进: orderId={}, status={}", record.getOrderId(), nextStatus);
    }
}
