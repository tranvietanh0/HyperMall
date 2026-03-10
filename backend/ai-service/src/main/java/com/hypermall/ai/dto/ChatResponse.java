package com.hypermall.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long sessionId;
    private Long messageId;
    private String message;
    private String intent;
    private Double confidence;
    private List<ProductSuggestion> suggestedProducts;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private Long productId;
        private String name;
        private String thumbnail;
        private Double price;
        private Double salePrice;
        private Double rating;
    }
}
