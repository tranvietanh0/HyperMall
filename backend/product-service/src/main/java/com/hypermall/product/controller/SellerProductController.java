package com.hypermall.product.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.product.dto.request.ProductRequest;
import com.hypermall.product.dto.response.ProductDetailResponse;
import com.hypermall.product.dto.response.ProductResponse;
import com.hypermall.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
@Tag(name = "Seller Product", description = "Seller product management APIs")
public class SellerProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create new product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ProductRequest request) {
        ProductDetailResponse product = productService.createProduct(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductDetailResponse product = productService.updateProduct(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        productService.deleteProduct(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Product deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get my products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getMyProducts(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        PageResponse<ProductResponse> products = productService.getProductsBySeller(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
