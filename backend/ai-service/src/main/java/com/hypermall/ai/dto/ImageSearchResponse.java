package com.hypermall.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchResponse {

    private List<SimilarProduct> products;
    private String detectedCategory;
    private List<String> detectedTags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimilarProduct {
        private Long productId;
        private String name;
        private String thumbnail;
        private Double price;
        private Double salePrice;
        private Double similarity;
    }
}
