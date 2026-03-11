package com.hypermall.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String message;
    private String sessionId;
    private List<SuggestedAction> suggestedActions;
    private List<ProductSuggestion> productSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedAction {
        private String type; // "link", "search", "category"
        private String label;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private Long productId;
        private String productName;
        private String thumbnail;
        private Double price;
    }
}
