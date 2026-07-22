package com.ccshop.common.core;

/**
 * 当前登录用户上下文（ThreadLocal）
 * 由各服务的拦截器从 JWT / 透传 Header 注入
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    public static void setUserId(Long id) {
        USER_ID.set(id);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String name) {
        USERNAME.set(name);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setRole(Integer role) {
        ROLE.set(role);
    }

    public static Integer getRole() {
        return ROLE.get();
    }

    public static boolean isLogin() {
        return USER_ID.get() != null;
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLE.remove();
    }
}
