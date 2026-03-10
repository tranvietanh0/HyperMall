package com.hypermall.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long variantId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    @Size(max = 9, message = "Maximum 9 images allowed")
    private List<String> images;

    @Size(max = 3, message = "Maximum 3 videos allowed")
    private List<String> videos;
}
