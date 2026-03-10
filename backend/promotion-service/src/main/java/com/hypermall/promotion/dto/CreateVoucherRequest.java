package com.hypermall.promotion.dto;

import com.hypermall.promotion.entity.VoucherType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVoucherRequest {

    @NotBlank(message = "Voucher code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, underscores, and hyphens")
    private String code;

    @NotBlank(message = "Voucher name is required")
    private String name;

    private String description;

    @NotNull(message = "Voucher type is required")
    private VoucherType type;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.01", message = "Value must be greater than 0")
    private BigDecimal value;

    private BigDecimal maxDiscount;

    @DecimalMin(value = "0", message = "Min order value cannot be negative")
    private BigDecimal minOrderValue;

    @Min(value = 0, message = "Usage limit cannot be negative")
    private Integer usageLimit = 0;

    @Min(value = 1, message = "User limit must be at least 1")
    private Integer userLimit = 1;

    private List<Long> applicableCategories;
    private List<Long> applicableProducts;
    private Long sellerId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
}
