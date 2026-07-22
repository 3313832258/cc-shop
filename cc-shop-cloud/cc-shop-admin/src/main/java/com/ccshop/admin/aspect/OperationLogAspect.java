package com.ccshop.admin.aspect;

import com.ccshop.admin.entity.OperationLog;
import com.ccshop.admin.mapper.OperationLogMapper;
import com.ccshop.common.core.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.ccshop.admin.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        com.ccshop.admin.annotation.OperationLog annotation =
                signature.getMethod().getAnnotation(com.ccshop.admin.annotation.OperationLog.class);

        // 构建日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setOperatorId(UserContext.getUserId());
        operationLog.setOperatorName(UserContext.getUsername());
        operationLog.setOperationType(annotation.operationType());
        operationLog.setModule(annotation.module());
        operationLog.setDescription(annotation.description());
        operationLog.setCreatedAt(LocalDateTime.now());

        // 获取请求信息
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setIp(getClientIp(request));
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }

        // 获取目标ID
        try {
            Object targetId = extractTargetId(joinPoint, annotation.targetIdParam());
            if (targetId != null) {
                operationLog.setTargetId(Long.valueOf(targetId.toString()));
            }
        } catch (Exception e) {
            log.debug("提取目标ID失败", e);
        }

        // 记录请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Map<String, Object> params = new HashMap<>();
                String[] paramNames = signature.getParameterNames();
                for (int i = 0; i < args.length; i++) {
                    if (paramNames != null && i < paramNames.length) {
                        params.put(paramNames[i], args[i]);
                    }
                }
                operationLog.setRequestParams(objectMapper.writeValueAsString(params));
            }
        } catch (Exception e) {
            log.debug("序列化请求参数失败", e);
        }

        // 执行方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            operationLog.setResult("SUCCESS");
        } catch (Throwable e) {
            operationLog.setResult("FAIL");
            operationLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // 异步保存日志
            try {
                operationLogMapper.insert(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }

        return result;
    }

    /**
     * 从参数中提取目标ID
     */
    private Object extractTargetId(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || args == null) {
            return null;
        }

        for (int i = 0; i < paramNames.length; i++) {
            if (paramName.equals(paramNames[i])) {
                return args[i];
            }
        }

        return null;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}