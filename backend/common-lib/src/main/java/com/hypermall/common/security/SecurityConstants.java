package com.hypermall.common.security;

public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // JWT Constants
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "authorities";
    public static final String USER_ID_KEY = "userId";

    // Token expiration times (in milliseconds)
    public static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Public endpoints (no authentication required)
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/products/**",
            "/api/categories/**",
            "/api/search/**",
            "/api/brands/**",
            "/api/shops/**",
            "/api/reviews/product/**",
            "/api/flash-sales/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // Admin only endpoints
    public static final String[] ADMIN_URLS = {
            "/api/admin/**"
    };

    // Seller only endpoints
    public static final String[] SELLER_URLS = {
            "/api/seller/**"
    };
}
