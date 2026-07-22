package com.ccshop.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("logistics_step")
public class LogisticsStep {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long logisticsId;
    private String description;
    private String location;
    private LocalDateTime timestamp;
}
