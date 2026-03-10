package com.hypermall.promotion.dto;

import com.hypermall.promotion.entity.VoucherType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApplyVoucherResponse {
    private String code;
    private String name;
    private VoucherType type;
    private BigDecimal discountAmount;
    private BigDecimal shippingDiscount;
    private BigDecimal totalDiscount;
    private boolean valid;
    private String message;
}
