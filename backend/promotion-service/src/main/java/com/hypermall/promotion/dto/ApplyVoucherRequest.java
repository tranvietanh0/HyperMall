package com.hypermall.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApplyVoucherRequest {

    @NotBlank(message = "Voucher code is required")
    private String code;

    @NotNull(message = "Order amount is required")
    private BigDecimal orderAmount;

    private BigDecimal shippingFee;

    private List<Long> productIds;
    private List<Long> categoryIds;
}
