package com.ccshop.common.tracker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户行为埋点事件（为后续 ML 推荐采集数据）。
 * 前端 / Gateway 上报 → MQ → cc-user 批量落库 user_action_log。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActionEvent implements Serializable {

    /** 行为类型：view / click / cart / order / favorite / search */
    private String action;

    private Long userId;

    /** 目标类型：product / order / coupon / category / keyword */
    private String targetType;

    private Long targetId;

    /** 扩展信息（JSON 字符串） */
    private String extra;

    /** 事件时间戳（毫秒） */
    private Long timestamp;
}
