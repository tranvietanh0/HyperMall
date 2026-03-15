package com.hypermall.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitConfigTest {

    @Test
    void userKeyResolverShouldPreferUserIdHeader() {
        RateLimitConfig config = new RateLimitConfig();
        KeyResolver resolver = config.userKeyResolver();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header("X-User-Id", "42")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );

        String resolvedKey = resolver.resolve(exchange).block();

        assertEquals("42", resolvedKey);
    }

    @Test
    void ipKeyResolverShouldUseRemoteAddress() {
        RateLimitConfig config = new RateLimitConfig();
        KeyResolver resolver = config.ipKeyResolver();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );

        String resolvedKey = resolver.resolve(exchange).block();

        assertEquals("127.0.0.1", resolvedKey);
    }

    @Test
    void ipKeyResolverShouldUseTrustedForwardedHeaderWhenEnabled() {
        RateLimitConfig config = new RateLimitConfig();
        ReflectionTestUtils.setField(config, "trustForwardedFor", true);
        ReflectionTestUtils.setField(config, "maxTrustedIndex", 1);
        KeyResolver resolver = config.ipKeyResolver();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );

        String resolvedKey = resolver.resolve(exchange).block();

        assertEquals("10.0.0.1", resolvedKey);
    }
}
