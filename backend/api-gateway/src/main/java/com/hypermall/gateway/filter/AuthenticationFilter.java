package com.hypermall.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class AuthenticationFilter implements GatewayFilter {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Public endpoints that don't require authentication
    private final List<PublicRoute> protectedOverrides = List.of(
            new PublicRoute(null, "/api/sellers/register"),
            new PublicRoute(null, "/api/sellers/me/**"),
            new PublicRoute(null, "/api/sellers/*/follow"),
            new PublicRoute(null, "/api/sellers/*/following"),
            new PublicRoute(null, "/api/vouchers/my-vouchers"),
            new PublicRoute(null, "/api/vouchers/claim/**"),
            new PublicRoute(null, "/api/vouchers/apply"),
            new PublicRoute(null, "/api/vouchers/*/status"),
            new PublicRoute(null, "/api/flash-sales/*/status")
    );

    private final List<PublicRoute> publicRoutes = List.of(
            new PublicRoute(null, "/api/auth/**"),
            new PublicRoute(HttpMethod.GET, "/api/products"),
            new PublicRoute(HttpMethod.GET, "/api/products/**"),
            new PublicRoute(HttpMethod.GET, "/api/categories"),
            new PublicRoute(HttpMethod.GET, "/api/categories/**"),
            new PublicRoute(HttpMethod.GET, "/api/brands"),
            new PublicRoute(HttpMethod.GET, "/api/brands/**"),
            new PublicRoute(HttpMethod.GET, "/api/search/**"),
            new PublicRoute(HttpMethod.GET, "/api/shops"),
            new PublicRoute(HttpMethod.GET, "/api/shops/**"),
            new PublicRoute(HttpMethod.GET, "/api/sellers"),
            new PublicRoute(HttpMethod.GET, "/api/sellers/*"),
            new PublicRoute(HttpMethod.GET, "/api/sellers/slug/*"),
            new PublicRoute(HttpMethod.GET, "/api/reviews/product/**"),
            new PublicRoute(HttpMethod.GET, "/api/flash-sales"),
            new PublicRoute(HttpMethod.GET, "/api/flash-sales/**"),
            new PublicRoute(HttpMethod.GET, "/api/vouchers"),
            new PublicRoute(HttpMethod.GET, "/api/vouchers/*"),
            new PublicRoute(HttpMethod.GET, "/api/vouchers/code/*"),
            new PublicRoute(HttpMethod.GET, "/api/vouchers/available"),
            new PublicRoute(HttpMethod.GET, "/api/payments/vnpay/callback"),
            new PublicRoute(HttpMethod.POST, "/api/payments/momo/callback"),
            new PublicRoute(HttpMethod.POST, "/api/payments/zalopay/callback"),
            new PublicRoute(HttpMethod.GET, "/actuator/health"),
            new PublicRoute(HttpMethod.GET, "/actuator/info"),
            new PublicRoute(HttpMethod.GET, "/v3/api-docs/**"),
            new PublicRoute(HttpMethod.GET, "/swagger-ui/**"),
            new PublicRoute(HttpMethod.GET, "/swagger-ui.html")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = sanitizeTrustedHeaders(exchange.getRequest());
        String path = request.getPath().value();

        // Skip authentication for public endpoints
        if (isPublicPath(request.getMethod(), path)) {
            return chain.filter(exchange.mutate().request(request).build());
        }

        // Check for Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = validateToken(token);
            String userEmail = claims.getSubject();
            Object userIdClaim = claims.get("userId");
            String authorities = claims.get("authorities", String.class);

            if (userEmail == null || userIdClaim == null || authorities == null || authorities.isBlank()) {
                return onError(exchange, "Invalid token claims", HttpStatus.UNAUTHORIZED);
            }

            // Add user information to headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(normalizeUserId(userIdClaim)))
                    .header("X-User-Email", userEmail)
                    .header("X-User-Roles", authorities)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(HttpMethod method, String path) {
        if (matchesRoute(protectedOverrides, method, path)) {
            return false;
        }

        return matchesRoute(publicRoutes, method, path);
    }

    private boolean matchesRoute(List<PublicRoute> routes, HttpMethod method, String path) {
        return routes.stream().anyMatch(route -> route.matches(method, path, pathMatcher));
    }

    private Claims validateToken(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private ServerHttpRequest sanitizeTrustedHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Email");
                    headers.remove("X-User-Roles");
                })
                .build();
    }

    private Long normalizeUserId(Object userIdClaim) {
        if (userIdClaim instanceof Long userId) {
            return userId;
        }

        if (userIdClaim instanceof Integer userId) {
            return userId.longValue();
        }

        if (userIdClaim instanceof String userId && !userId.isBlank()) {
            return Long.parseLong(userId);
        }

        throw new IllegalArgumentException("Invalid userId claim type: " + Objects.toString(userIdClaim));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    private record PublicRoute(HttpMethod method, String pattern) {
        private boolean matches(HttpMethod requestMethod, String path, AntPathMatcher pathMatcher) {
            return (method == null || method == requestMethod) && pathMatcher.match(pattern, path);
        }
    }
}
