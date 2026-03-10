package com.hypermall.payment.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {

    @Positive(message = "Refund amount must be positive")
    private BigDecimal amount; // null for full refund

    private String reason;
}
