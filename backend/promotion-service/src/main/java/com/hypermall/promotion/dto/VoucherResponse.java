package com.hypermall.promotion.dto;

import com.hypermall.promotion.entity.VoucherStatus;
import com.hypermall.promotion.entity.VoucherType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private VoucherType type;
    private BigDecimal value;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer userLimit;
    private Long sellerId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;
    private boolean valid;
    private boolean claimed; // For user-specific responses
    private boolean used;    // For user-specific responses
    private LocalDateTime createdAt;
}
