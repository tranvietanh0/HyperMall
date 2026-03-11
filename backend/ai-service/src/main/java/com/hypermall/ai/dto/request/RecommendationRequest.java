package com.hypermall.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    private Long userId;
    private Long productId;
    private Long categoryId;
    private List<Long> viewedProductIds;
    private List<Long> purchasedProductIds;
    private List<String> searchHistory;
    private int limit;
    private String type; // "similar", "frequently_bought", "personalized", "trending"
}
