package com.hypermall.gateway.config;

import com.hypermall.gateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/auth/**", "/api/users/**", "/api/admin/users/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://user-service"))

                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/products/**", "/api/categories/**", "/api/brands/**",
                                "/api/seller/products/**", "/api/admin/products/**",
                                "/api/admin/categories/**", "/api/attributes/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://product-service"))

                // Cart Service Routes
                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://cart-service"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**", "/api/seller/orders/**", "/api/admin/orders/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://order-service"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://payment-service"))

                // Inventory Service Routes
                .route("inventory-service", r -> r
                        .path("/api/inventory/**", "/api/seller/inventory/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://inventory-service"))

                // Shipping Service Routes
                .route("shipping-service", r -> r
                        .path("/api/shipping/**", "/api/seller/shipping/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://shipping-service"))

                // Promotion Service Routes
                .route("promotion-service", r -> r
                        .path("/api/vouchers/**", "/api/flash-sales/**",
                                "/api/admin/vouchers/**", "/api/admin/flash-sales/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://promotion-service"))

                // Review Service Routes
                .route("review-service", r -> r
                        .path("/api/reviews/**", "/api/seller/reviews/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://review-service"))

                // Search Service Routes
                .route("search-service", r -> r
                        .path("/api/search/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://search-service"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://notification-service"))

                // Media Service Routes
                .route("media-service", r -> r
                        .path("/api/media/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://media-service"))

                // Seller Service Routes
                .route("seller-service", r -> r
                        .path("/api/sellers/**", "/api/shops/**", "/api/admin/sellers/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("lb://seller-service"))

                .build();
    }
}
