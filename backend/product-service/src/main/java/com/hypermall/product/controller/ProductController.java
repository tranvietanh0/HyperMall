package com.hypermall.product.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.product.dto.response.ProductDetailResponse;
import com.hypermall.product.dto.response.ProductResponse;
import com.hypermall.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product browsing APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products with filters")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            Pageable pageable) {
        PageResponse<ProductResponse> products = productService.getProducts(
                keyword, categoryId, brandId, minPrice, maxPrice, minRating, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        PageResponse<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsBySeller(
            @PathVariable Long sellerId,
            Pageable pageable) {
        PageResponse<ProductResponse> products = productService.getProductsBySeller(sellerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
