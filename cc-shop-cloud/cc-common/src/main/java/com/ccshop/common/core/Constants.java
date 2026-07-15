package com.ccshop.common.core;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {}

    /** JWT Header 名 */
    public static final String AUTH_HEADER = "Authorization";
    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    /** JWT Claim: 用户ID */
    public static final String CLAIM_USER_ID = "uid";
    /** JWT Claim: 用户名 */
    public static final String CLAIM_USERNAME = "username";
    /** Feign 透传 Header: 用户ID */
    public static final String HEADER_USER_ID = "X-User-Id";
    /** Feign 透传 Header: 用户名 */
    public static final String HEADER_USERNAME = "X-Username";

    /** Redis Key 前缀 */
    public static final String CACHE_PRODUCT_DETAIL = "cc:product:detail:";
    public static final String CACHE_PRODUCT_LIST = "cc:product:list:";
    public static final String CACHE_CATEGORY_TREE = "cc:category:tree";
    public static final String CACHE_BRAND_LIST = "cc:brand:list";

    /** 购物车 Redis Hash Key 前缀 */
    public static final String CART_PREFIX = "cart:";
    /** 优惠券剩余数量 Redis Key 前缀 */
    public static final String COUPON_REMAINING = "coupon:remaining:";

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;
}
