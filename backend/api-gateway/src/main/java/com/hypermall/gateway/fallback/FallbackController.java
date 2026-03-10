package com.hypermall.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return Mono.just(createFallbackResponse("User Service"));
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return Mono.just(createFallbackResponse("Product Service"));
    }

    @GetMapping("/cart-service")
    public Mono<ResponseEntity<Map<String, Object>>> cartServiceFallback() {
        return Mono.just(createFallbackResponse("Cart Service"));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        return Mono.just(createFallbackResponse("Order Service"));
    }

    @GetMapping("/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        return Mono.just(createFallbackResponse("Payment Service"));
    }

    @GetMapping("/inventory-service")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryServiceFallback() {
        return Mono.just(createFallbackResponse("Inventory Service"));
    }

    @GetMapping("/shipping-service")
    public Mono<ResponseEntity<Map<String, Object>>> shippingServiceFallback() {
        return Mono.just(createFallbackResponse("Shipping Service"));
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return Mono.just(createFallbackResponse("Notification Service"));
    }

    @GetMapping("/search-service")
    public Mono<ResponseEntity<Map<String, Object>>> searchServiceFallback() {
        return Mono.just(createFallbackResponse("Search Service"));
    }

    @GetMapping("/review-service")
    public Mono<ResponseEntity<Map<String, Object>>> reviewServiceFallback() {
        return Mono.just(createFallbackResponse("Review Service"));
    }

    @GetMapping("/promotion-service")
    public Mono<ResponseEntity<Map<String, Object>>> promotionServiceFallback() {
        return Mono.just(createFallbackResponse("Promotion Service"));
    }

    @GetMapping("/media-service")
    public Mono<ResponseEntity<Map<String, Object>>> mediaServiceFallback() {
        return Mono.just(createFallbackResponse("Media Service"));
    }

    @GetMapping("/default")
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback() {
        return Mono.just(createFallbackResponse("Service"));
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "SERVICE_UNAVAILABLE");
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
