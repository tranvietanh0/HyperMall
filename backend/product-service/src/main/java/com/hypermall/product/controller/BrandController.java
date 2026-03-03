package com.hypermall.product.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.product.dto.response.BrandResponse;
import com.hypermall.product.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "Brand management APIs")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Get all brands")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(brands));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable Long id) {
        BrandResponse brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success(brand));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get brand by slug")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandBySlug(@PathVariable String slug) {
        BrandResponse brand = brandService.getBrandBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(brand));
    }
}
