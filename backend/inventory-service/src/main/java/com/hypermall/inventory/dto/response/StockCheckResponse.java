package com.hypermall.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {

    private boolean allInStock;
    private List<StockCheckItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockCheckItem {
        private Long productId;
        private Long variantId;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private boolean inStock;
    }
}
