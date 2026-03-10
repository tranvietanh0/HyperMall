package com.hypermall.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productId;
    private Long variantId;
    private Long sellerId;
    private String sku;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Boolean isTrackQuantity;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
