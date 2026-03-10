package com.hypermall.review.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.review.dto.*;
import com.hypermall.review.entity.ReviewStatus;
import com.hypermall.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a new review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.createReview(
                currentUser.getId(),
                currentUser.getUsername(), // Email as username
                null, // Avatar URL can be fetched from user service
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created", review));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        ReviewResponse review = reviewService.getReviewById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "false") boolean withImages,
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, rating, withImages, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/product/{productId}/statistics")
    @Operation(summary = "Get review statistics for a product")
    public ResponseEntity<ApiResponse<ReviewStatisticsResponse>> getProductStatistics(@PathVariable Long productId) {
        ReviewStatisticsResponse stats = reviewService.getProductStatistics(productId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/my-reviews")
    @Operation(summary = "Get current user's reviews", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getUserReviews(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get reviews for seller's products", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getSellerReviews(
            @PathVariable Long sellerId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getSellerReviews(sellerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse review = reviewService.updateReview(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Review updated", review));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        reviewService.deleteReview(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Add seller reply to a review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ReviewResponse>> addSellerReply(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SellerReplyRequest request) {
        ReviewResponse review = reviewService.addSellerReply(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Reply added", review));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Toggle like on a review", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        boolean liked = reviewService.toggleLike(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(liked ? "Review liked" : "Review unliked", liked));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update review status (Admin)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReviewStatus(
            @PathVariable Long id,
            @RequestParam ReviewStatus status) {
        ReviewResponse review = reviewService.updateReviewStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Review status updated", review));
    }
}
