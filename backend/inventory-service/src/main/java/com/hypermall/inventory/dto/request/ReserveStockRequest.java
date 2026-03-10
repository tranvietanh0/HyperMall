package com.hypermall.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotEmpty(message = "Items are required")
    @Valid
    private List<ReserveItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserveItem {
        @NotNull(message = "Product ID is required")
        private Long productId;

        private Long variantId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}
