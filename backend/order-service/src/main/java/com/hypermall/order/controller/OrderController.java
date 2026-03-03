package com.hypermall.order.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.order.dto.*;
import com.hypermall.order.entity.OrderStatus;
import com.hypermall.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management for buyers")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDetailResponse order = orderService.createOrder(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getUserOrders(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orders)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail by ID")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        OrderDetailResponse order = orderService.getOrderById(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/tracking/{orderNumber}")
    @Operation(summary = "Track order by order number")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> trackOrder(
            @PathVariable String orderNumber) {
        OrderDetailResponse order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderDetailResponse order = orderService.cancelOrder(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
