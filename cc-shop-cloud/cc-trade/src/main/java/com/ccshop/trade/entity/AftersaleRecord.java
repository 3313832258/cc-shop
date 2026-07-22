package com.ccshop.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("aftersale_record")
public class AftersaleRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    /** refund=仅退款, return_refund=退货退款 */
    private String type;
    private String reason;
    /** 0=待审 1=通过 2=拒绝 3=处理中 4=完成 */
    private Integer status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
