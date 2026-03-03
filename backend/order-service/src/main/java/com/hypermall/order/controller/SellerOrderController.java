package com.hypermall.order.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.order.dto.OrderDetailResponse;
import com.hypermall.order.dto.OrderResponse;
import com.hypermall.order.entity.OrderStatus;
import com.hypermall.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
@Tag(name = "Seller Orders", description = "Order management for sellers")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get orders for current seller")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getSellerOrders(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getSellerOrders(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(orders)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        OrderDetailResponse order = orderService.updateOrderStatus(currentUser.getId(), id, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }
}
