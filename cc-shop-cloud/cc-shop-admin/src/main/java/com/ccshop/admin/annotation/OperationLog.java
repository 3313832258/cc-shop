package com.ccshop.admin.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在 Controller 方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module();

    /**
     * 操作类型
     */
    String operationType();

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 目标ID参数名（从请求参数中提取）
     */
    String targetIdParam() default "id";
}