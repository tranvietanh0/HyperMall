package com.hypermall.ai.controller;

import com.hypermall.ai.dto.RecommendationRequest;
import com.hypermall.ai.dto.RecommendationResponse;
import com.hypermall.ai.dto.TrackBehaviorRequest;
import com.hypermall.ai.service.RecommendationService;
import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
@Tag(name = "AI Recommendations", description = "Product recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personalized")
    @Operation(summary = "Get personalized recommendations", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<RecommendationResponse>> getPersonalized(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long userId = currentUser != null ? currentUser.getId() : null;

        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .type(RecommendationRequest.RecommendationType.PERSONALIZED)
                .limit(limit)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/similar/{productId}")
    @Operation(summary = "Get similar products")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getSimilar(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        RecommendationRequest request = RecommendationRequest.builder()
                .productId(productId)
                .type(RecommendationRequest.RecommendationType.SIMILAR_PRODUCTS)
                .limit(limit)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/frequently-bought/{productId}")
    @Operation(summary = "Get frequently bought together products")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getFrequentlyBought(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        RecommendationRequest request = RecommendationRequest.builder()
                .productId(productId)
                .type(RecommendationRequest.RecommendationType.FREQUENTLY_BOUGHT)
                .limit(limit)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending products")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getTrending(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.TRENDING)
                .limit(limit)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/new-arrivals")
    @Operation(summary = "Get new arrivals")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getNewArrivals(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        RecommendationRequest request = RecommendationRequest.builder()
                .type(RecommendationRequest.RecommendationType.NEW_ARRIVALS)
                .limit(limit)
                .build();

        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/track")
    @Operation(summary = "Track user behavior", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> trackBehavior(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TrackBehaviorRequest request) {
        recommendationService.trackBehavior(
                currentUser.getId(),
                request.getProductId(),
                request.getBehaviorType(),
                request.getSearchQuery(),
                request.getCategoryId(),
                request.getBrandId()
        );
        return ResponseEntity.ok(ApiResponse.success("Behavior tracked", null));
    }
}
