package com.ccshop.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Sentinel Gateway 集成配置。
 * 自定义被限流时的返回结构。
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        BlockRequestHandler handler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                log.warn("Sentinel 限流触发: {}", exchange.getRequest().getURI().getPath());
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
            }
        };
        GatewayCallbackManager.setBlockHandler(handler);
    }
}
