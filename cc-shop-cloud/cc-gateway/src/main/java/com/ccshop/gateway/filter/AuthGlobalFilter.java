package com.ccshop.gateway.filter;

import com.ccshop.common.core.Constants;
import com.ccshop.common.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 全局鉴权过滤器。
 * 白名单路径直接放行，其余请求校验 JWT 并将用户身份写入透传 Header。
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 无需 JWT 的路径白名单 */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/auth/login",
            "/api/user/auth/register",
            "/api/user/auth/captcha",
            "/api/user/auth/sms/send",
            "/api/user/auth/sms/login",
            "/api/user/auth/reset-password",
            "/api/user/auth/refresh",
            "/api/user/action",
            "/api/product/category",
            "/api/product/brand",
            "/api/product/detail",
            "/api/product/list",
            "/api/product/search",
            "/api/admin/auth/login",
            "/api/ai/chat",
            "/api/ai/chat/stream",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单放行（精确前缀匹配，避免 /auth/login 匹配到 /auth/login-logs）
        for (String white : WHITE_LIST) {
            if (path.equals(white) || path.startsWith(white + "/") || path.startsWith(white + "?")) {
                return chain.filter(exchange);
            }
        }

        // 提取 JWT
        String token = request.getHeaders().getFirst(Constants.AUTH_HEADER);
        if (token == null || token.isBlank()) {
            return unauthorized(exchange, "缺少授权凭证");
        }

        try {
            Claims claims = JwtUtil.parse(token);
            Long userId = null;
            Object uid = claims.get(Constants.CLAIM_USER_ID);
            if (uid instanceof Number) userId = ((Number) uid).longValue();
            else if (uid != null) userId = Long.valueOf(uid.toString());

            String username = claims.get(Constants.CLAIM_USERNAME, String.class);
            int role = 0;
            Object roleObj = claims.get(Constants.CLAIM_ROLE);
            if (roleObj instanceof Number) role = ((Number) roleObj).intValue();

            if (userId == null) {
                return unauthorized(exchange, "无效的用户凭证");
            }

            // 管理员接口鉴权：/api/user/admin/** 需要管理员角色
            if (path.startsWith("/api/user/admin/") && role < 2) {
                return forbidden(exchange, "需要管理员权限");
            }

            // 管理后台接口鉴权：/api/admin/** 需要商家或管理员角色
            if (path.startsWith("/api/admin/") && role < 1) {
                return forbidden(exchange, "需要商家或管理员身份");
            }

            // 写入透传 Header 供下游服务使用
            ServerHttpRequest mutated = request.mutate()
                    .header(Constants.HEADER_USER_ID, String.valueOf(userId))
                    .header(Constants.HEADER_USERNAME, username != null ? username : "")
                    .header(Constants.HEADER_ROLE, String.valueOf(role))
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (Exception e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            return unauthorized(exchange, "授权凭证无效或已过期");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        String body = "{\"code\":401,\"message\":\"" + msg + "\",\"data\":null}";
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        String body = "{\"code\":403,\"message\":\"" + msg + "\",\"data\":null}";
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
