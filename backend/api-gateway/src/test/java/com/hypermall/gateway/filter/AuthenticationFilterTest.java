package com.hypermall.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationFilterTest {

    private static final String JWT_SECRET = Base64.getEncoder()
            .encodeToString("hypermall-gateway-test-secret-key-123456".getBytes(StandardCharsets.UTF_8));

    @Test
    void shouldStripTrustedHeadersOnPublicRoutes() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/products")
                        .header("X-User-Id", "spoofed")
                        .header("X-User-Email", "spoofed@example.com")
                        .header("X-User-Roles", "ADMIN")
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasInvoked);
        assertNotNull(chain.forwardedRequest);
        assertNull(chain.forwardedRequest.getHeaders().getFirst("X-User-Id"));
        assertNull(chain.forwardedRequest.getHeaders().getFirst("X-User-Email"));
        assertNull(chain.forwardedRequest.getHeaders().getFirst("X-User-Roles"));
    }

    @Test
    void shouldRejectProtectedRoutesWithoutBearerToken() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/1").build()
        );

        filter.filter(exchange, chain).block();

        assertFalse(chain.wasInvoked);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldReplaceTrustedHeadersForAuthenticatedRoutes() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        String token = createToken(123L, "buyer@hypermall.local", "BUYER");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Id", "spoofed")
                        .header("X-User-Email", "spoofed@example.com")
                        .header("X-User-Roles", "ADMIN")
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasInvoked);
        assertNotNull(chain.forwardedRequest);
        assertEquals("123", chain.forwardedRequest.getHeaders().getFirst("X-User-Id"));
        assertEquals("buyer@hypermall.local", chain.forwardedRequest.getHeaders().getFirst("X-User-Email"));
        assertEquals("BUYER", chain.forwardedRequest.getHeaders().getFirst("X-User-Roles"));
    }

    @Test
    void shouldAllowPublicSellerReadRouteWithoutAuthentication() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/sellers/123").build()
        );

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasInvoked);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldKeepSellerSelfRouteProtected() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/sellers/me").build()
        );

        filter.filter(exchange, chain).block();

        assertFalse(chain.wasInvoked);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowPublicVoucherLookupWithoutAuthentication() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/vouchers/code/SAVE10").build()
        );

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasInvoked);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldKeepVoucherClaimRouteProtected() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/vouchers/claim/SAVE10").build()
        );

        filter.filter(exchange, chain).block();

        assertFalse(chain.wasInvoked);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowConfiguredPaymentCallbackMethod() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/payments/momo/callback").build()
        );

        filter.filter(exchange, chain).block();

        assertTrue(chain.wasInvoked);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectUnexpectedPaymentCallbackMethod() {
        AuthenticationFilter filter = createFilter();
        RecordingGatewayFilterChain chain = new RecordingGatewayFilterChain();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.delete("/api/payments/momo/callback").build()
        );

        filter.filter(exchange, chain).block();

        assertFalse(chain.wasInvoked);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    private AuthenticationFilter createFilter() {
        AuthenticationFilter filter = new AuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", JWT_SECRET);
        return filter;
    }

    private String createToken(Long userId, String username, String authorities) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    private static final class RecordingGatewayFilterChain implements org.springframework.cloud.gateway.filter.GatewayFilterChain {
        private boolean wasInvoked;
        private ServerHttpRequest forwardedRequest;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.wasInvoked = true;
            this.forwardedRequest = exchange.getRequest();
            return Mono.empty();
        }
    }
}
