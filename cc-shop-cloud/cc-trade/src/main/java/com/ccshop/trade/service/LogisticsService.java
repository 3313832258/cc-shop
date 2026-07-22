package com.ccshop.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.trade.dto.LogisticsVO;
import com.ccshop.trade.entity.LogisticsRecord;
import com.ccshop.trade.entity.LogisticsStep;
import com.ccshop.trade.mapper.LogisticsRecordMapper;
import com.ccshop.trade.mapper.LogisticsStepMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsRecordMapper logisticsRecordMapper;
    private final LogisticsStepMapper logisticsStepMapper;

    public LogisticsVO getLogistics(Long orderId) {
        LogisticsRecord record = logisticsRecordMapper.selectOne(
                new LambdaQueryWrapper<LogisticsRecord>()
                        .eq(LogisticsRecord::getOrderId, orderId));
        if (record == null) {
            throw new BusinessException(404, "物流信息不存在");
        }

        List<LogisticsStep> steps = logisticsStepMapper.selectList(
                new LambdaQueryWrapper<LogisticsStep>()
                        .eq(LogisticsStep::getLogisticsId, record.getId())
                        .orderByDesc(LogisticsStep::getTimestamp));

        LogisticsVO vo = new LogisticsVO();
        vo.setOrderId(record.getOrderId());
        vo.setCarrier(record.getCarrier());
        vo.setTrackingNo(record.getTrackingNo());
        vo.setStatus(record.getStatus());
        vo.setStatusText(getStatusText(record.getStatus()));
        vo.setCurrentLocation(record.getCurrentLocation());
        vo.setEstimatedDelivery(record.getEstimatedDelivery());

        List<LogisticsVO.StepVO> stepVOs = new ArrayList<>();
        for (LogisticsStep step : steps) {
            LogisticsVO.StepVO stepVO = new LogisticsVO.StepVO();
            stepVO.setDescription(step.getDescription());
            stepVO.setLocation(step.getLocation());
            stepVO.setTimestamp(step.getTimestamp());
            stepVOs.add(stepVO);
        }
        vo.setSteps(stepVOs);
        return vo;
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "待发货";
            case 1 -> "已揽收";
            case 2 -> "运输中";
            case 3 -> "派送中";
            case 4 -> "已签收";
            default -> "未知";
        };
    }
}
