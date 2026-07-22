package com.ccshop.common.web;

import com.ccshop.common.core.Constants;
import com.ccshop.common.core.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器：从透传 Header 注入当前用户到 ThreadLocal。
 * Gateway 鉴权后下发这些 Header，下游服务只需信任并解析。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uid = request.getHeader(Constants.HEADER_USER_ID);
        if (uid != null && !uid.isEmpty()) {
            try {
                UserContext.setUserId(Long.valueOf(uid));
            } catch (NumberFormatException ignore) {
            }
        }
        String name = request.getHeader(Constants.HEADER_USERNAME);
        if (name != null) {
            UserContext.setUsername(name);
        }
        String role = request.getHeader(Constants.HEADER_ROLE);
        if (role != null && !role.isEmpty()) {
            try {
                UserContext.setRole(Integer.valueOf(role));
            } catch (NumberFormatException ignore) {
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
