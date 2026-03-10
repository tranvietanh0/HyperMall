package com.hypermall.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    private Long userId;
    private Long productId;
    private Long categoryId;
    private RecommendationType type;
    private Integer limit;

    public enum RecommendationType {
        PERSONALIZED,      // Dựa trên hành vi người dùng
        SIMILAR_PRODUCTS,  // Sản phẩm tương tự
        FREQUENTLY_BOUGHT, // Thường mua cùng
        TRENDING,          // Xu hướng
        NEW_ARRIVALS       // Sản phẩm mới
    }
}
