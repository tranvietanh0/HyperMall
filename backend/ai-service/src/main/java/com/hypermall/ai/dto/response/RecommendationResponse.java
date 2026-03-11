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
public class RecommendationResponse {

    private String type;
    private List<RecommendedProduct> products;
    private String reason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedProduct {
        private Long productId;
        private String name;
        private String thumbnail;
        private BigDecimal price;
        private BigDecimal salePrice;
        private Double rating;
        private Integer totalSold;
        private Double relevanceScore;
        private String recommendReason;
    }
}
