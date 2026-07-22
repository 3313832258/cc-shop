package com.ccshop.trade.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LogisticsVO {
    private Long orderId;
    private String carrier;
    private String trackingNo;
    private Integer status;
    private String statusText;
    private String currentLocation;
    private LocalDateTime estimatedDelivery;
    private List<StepVO> steps;

    @Data
    public static class StepVO {
        private String description;
        private String location;
        private LocalDateTime timestamp;
    }
}
