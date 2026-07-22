package com.ccshop.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人用户名
     */
    private String operatorName;

    /**
     * 操作类型：CREATE/UPDATE/DELETE/APPROVE/REJECT
     */
    private String operationType;

    /**
     * 操作模块：product/order/aftersale/coupon
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作的目标ID（如商品ID、订单ID）
     */
    private Long targetId;

    /**
     * 请求参数 JSON
     */
    private String requestParams;

    /**
     * 操作结果：SUCCESS/FAIL
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;
}