package com.hypermall.shipping.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.shipping.dto.request.CalculateShippingRequest;
import com.hypermall.shipping.dto.request.CreateShipmentRequest;
import com.hypermall.shipping.dto.response.ShipmentResponse;
import com.hypermall.shipping.dto.response.ShippingOptionResponse;
import com.hypermall.shipping.dto.response.TrackingResponse;
import com.hypermall.shipping.entity.ShipmentStatus;
import com.hypermall.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Shipping and logistics APIs")
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate shipping fee for all providers")
    public ResponseEntity<ApiResponse<List<ShippingOptionResponse>>> calculateShipping(
            @Valid @RequestBody CalculateShippingRequest request) {
        List<ShippingOptionResponse> options = shippingService.calculateShipping(request);
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @PostMapping("/shipments")
    @Operation(summary = "Create a new shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse shipment = shippingService.createShipment(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment created successfully", shipment));
    }

    @GetMapping("/shipments/order/{orderId}")
    @Operation(summary = "Get shipment by order ID")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByOrderId(@PathVariable Long orderId) {
        ShipmentResponse shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(shipment));
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number")
    public ResponseEntity<ApiResponse<TrackingResponse>> trackShipment(@PathVariable String trackingNumber) {
        TrackingResponse tracking = shippingService.trackShipment(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(tracking));
    }

    @GetMapping("/shipments/seller")
    @Operation(summary = "Get seller's shipments")
    public ResponseEntity<ApiResponse<PageResponse<ShipmentResponse>>> getSellerShipments(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        Page<ShipmentResponse> shipments = shippingService.getSellerShipments(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(shipments)));
    }

    @PutMapping("/shipments/{shipmentId}/status")
    @Operation(summary = "Update shipment status (webhook from provider)")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateShipmentStatus(
            @PathVariable Long shipmentId,
            @RequestParam ShipmentStatus status,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location) {
        ShipmentResponse shipment = shippingService.updateShipmentStatus(
                shipmentId, status,
                description != null ? description : status.name(),
                location);
        return ResponseEntity.ok(ApiResponse.success("Status updated", shipment));
    }
}
