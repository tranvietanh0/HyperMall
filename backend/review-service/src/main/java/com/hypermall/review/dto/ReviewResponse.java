package com.hypermall.review.dto;

import com.hypermall.review.entity.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private Long orderId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String content;
    private List<String> images;
    private List<String> videos;
    private Integer likeCount;
    private Boolean verifiedPurchase;
    private ReviewStatus status;
    private String sellerReply;
    private LocalDateTime sellerReplyAt;
    private LocalDateTime createdAt;
    private boolean liked; // For current user context
}
