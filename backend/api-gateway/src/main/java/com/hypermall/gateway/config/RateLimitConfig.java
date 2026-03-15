package com.hypermall.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    @Value("${app.rate-limit.replenish-rate:100}")
    private int replenishRate;

    @Value("${app.rate-limit.burst-capacity:200}")
    private int burstCapacity;

    @Value("${app.rate-limit.requested-tokens:1}")
    private int requestedTokens;

    @Value("${app.rate-limit.trust-forwarded-for:false}")
    private boolean trustForwardedFor;

    @Value("${app.rate-limit.max-trusted-index:1}")
    private int maxTrustedIndex;

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(replenishRate, burstCapacity, requestedTokens);
    }

    /**
     * Rate limit by IP address
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(resolveClientAddress(exchange));
    }

    /**
     * Rate limit by User ID (from JWT token)
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }
            return Mono.just(resolveClientAddress(exchange));
        };
    }

    /**
     * Rate limit by API path
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    private String resolveClientAddress(org.springframework.web.server.ServerWebExchange exchange) {
        if (trustForwardedFor) {
            return XForwardedRemoteAddressResolver.maxTrustedIndex(maxTrustedIndex)
                    .resolve(exchange)
                    .getAddress()
                    .getHostAddress();
        }

        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress()
                .getHostAddress();
    }
}
