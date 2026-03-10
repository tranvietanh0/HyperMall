package com.hypermall.promotion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateFlashSaleRequest {

    @NotBlank(message = "Flash sale name is required")
    private String name;

    private String description;
    private String bannerImage;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @NotEmpty(message = "At least one product is required")
    @Valid
    private List<FlashSaleProductRequest> products;

    @Data
    public static class FlashSaleProductRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        private Long variantId;

        @NotBlank(message = "Product name is required")
        private String productName;

        private String productImage;

        @NotNull(message = "Original price is required")
        @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
        private BigDecimal originalPrice;

        @NotNull(message = "Flash sale price is required")
        @DecimalMin(value = "0.01", message = "Flash sale price must be greater than 0")
        private BigDecimal flashSalePrice;

        @NotNull(message = "Stock limit is required")
        @Min(value = 1, message = "Stock limit must be at least 1")
        private Integer stockLimit;

        private Integer sortOrder = 0;
    }
}
