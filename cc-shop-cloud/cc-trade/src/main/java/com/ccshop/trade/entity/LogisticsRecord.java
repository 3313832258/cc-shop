package com.ccshop.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("logistics_record")
public class LogisticsRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String carrier;
    private String trackingNo;
    /** 0=待发货 1=已揽收 2=运输中 3=派送中 4=已签收 */
    private Integer status;
    private String currentLocation;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
}
