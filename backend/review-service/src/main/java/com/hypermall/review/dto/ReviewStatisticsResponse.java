package com.hypermall.review.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReviewStatisticsResponse {
    private Long productId;
    private Double averageRating;
    private Integer totalReviews;
    private Map<Integer, Integer> ratingDistribution; // rating -> count
    private Integer withImages;
    private Integer withVideos;
    private Integer verifiedPurchases;
}
