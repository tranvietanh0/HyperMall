package com.hypermall.payment.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.payment.dto.*;
import com.hypermall.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and gateway callbacks")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "Create a new payment", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        if (request.getClientIp() == null) {
            request.setClientIp(getClientIp(httpRequest));
        }
        PaymentResponse payment = paymentService.createPayment(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created", payment));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    // VNPay callback (GET - redirect from VNPay)
    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay payment callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnpayCallback(
            @RequestParam Map<String, String> params) {
        PaymentResponse payment = paymentService.handleVNPayCallback(params);
        return ResponseEntity.ok(ApiResponse.success("VNPay callback processed", payment));
    }

    // MoMo callback (POST - IPN from MoMo server)
    @PostMapping("/momo/callback")
    @Operation(summary = "MoMo payment IPN callback")
    public ResponseEntity<Map<String, Object>> momoCallback(
            @RequestBody MoMoCallbackRequest request) {
        Map<String, Object> result = paymentService.handleMoMoCallback(request);
        return ResponseEntity.ok(result);
    }

    // ZaloPay callback (POST - from ZaloPay server)
    @PostMapping("/zalopay/callback")
    @Operation(summary = "ZaloPay payment callback")
    public ResponseEntity<Map<String, Object>> zalopayCallback(
            @RequestBody ZaloPayCallbackRequest request) {
        Map<String, Object> result = paymentService.handleZaloPayCallback(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Initiate a refund for a payment", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        PaymentResponse payment = paymentService.refundPayment(id, request.getAmount(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Refund initiated", payment));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
