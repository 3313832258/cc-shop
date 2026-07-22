package com.ccshop.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flash_sale_activity")
public class FlashSaleActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 0=未开始 1=进行中 2=已结束 */
    private Integer status;
    private LocalDateTime createdAt;
}
