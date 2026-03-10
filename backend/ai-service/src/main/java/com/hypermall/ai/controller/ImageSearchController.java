package com.hypermall.ai.controller;

import com.hypermall.ai.dto.ImageSearchRequest;
import com.hypermall.ai.dto.ImageSearchResponse;
import com.hypermall.ai.service.ImageSearchService;
import com.hypermall.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/image-search")
@RequiredArgsConstructor
@Tag(name = "AI Image Search", description = "Visual product search")
public class ImageSearchController {

    private final ImageSearchService imageSearchService;

    @PostMapping
    @Operation(summary = "Search products by image")
    public ResponseEntity<ApiResponse<ImageSearchResponse>> searchByImage(
            @RequestBody ImageSearchRequest request) {
        ImageSearchResponse response = imageSearchService.searchByImage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/index/{productId}")
    @Operation(summary = "Index product embedding", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> indexProduct(
            @PathVariable Long productId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl) {
        imageSearchService.indexProductEmbedding(productId, name, description, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Product indexed", null));
    }

    @DeleteMapping("/index/{productId}")
    @Operation(summary = "Remove product embedding", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        imageSearchService.deleteProductEmbedding(productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from index", null));
    }
}
