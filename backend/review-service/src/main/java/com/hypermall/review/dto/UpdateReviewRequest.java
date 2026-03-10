package com.hypermall.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class UpdateReviewRequest {

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
