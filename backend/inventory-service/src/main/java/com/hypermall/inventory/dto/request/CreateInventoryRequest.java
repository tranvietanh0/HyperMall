package com.hypermall.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long variantId;

    private String sku;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be >= 0")
    private Integer quantity;

    @Min(value = 0, message = "Low stock threshold must be >= 0")
    private Integer lowStockThreshold = 10;

    private Boolean isTrackQuantity = true;
}
