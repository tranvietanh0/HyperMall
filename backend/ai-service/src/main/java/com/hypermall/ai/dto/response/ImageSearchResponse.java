package com.hypermall.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchResponse {

    private List<MatchedProduct> products;
    private List<String> detectedLabels;
    private String suggestedCategory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchedProduct {
        private Long productId;
        private String name;
        private String thumbnail;
        private BigDecimal price;
        private Double similarityScore;
    }
}
