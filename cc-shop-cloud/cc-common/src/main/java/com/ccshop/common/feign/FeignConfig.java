package com.ccshop.common.feign;

import com.ccshop.common.core.Constants;
import com.ccshop.common.core.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置：向下游服务透传当前用户身份（X-User-Id / X-Username）。
 * Gateway 解析 JWT 后写入这些 Header，服务间调用时 Feign 拦截器再透传。
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userContextInterceptor() {
        return template -> {
            Long uid = UserContext.getUserId();
            if (uid != null) {
                template.header(Constants.HEADER_USER_ID, String.valueOf(uid));
            }
            String name = UserContext.getUsername();
            if (name != null) {
                template.header(Constants.HEADER_USERNAME, name);
            }
        };
    }
}
