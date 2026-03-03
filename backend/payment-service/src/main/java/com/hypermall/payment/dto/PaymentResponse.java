package com.hypermall.payment.dto;

import com.hypermall.payment.entity.PaymentMethod;
import com.hypermall.payment.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private String paymentUrl;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
