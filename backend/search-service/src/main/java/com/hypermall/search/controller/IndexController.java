package com.hypermall.search.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.search.dto.IndexProductRequest;
import com.hypermall.search.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search/index")
@RequiredArgsConstructor
@Tag(name = "Search Index", description = "Product indexing operations")
public class IndexController {

    private final ProductSearchService productSearchService;

    @PostMapping("/product")
    @Operation(summary = "Index a single product", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> indexProduct(@Valid @RequestBody IndexProductRequest request) {
        productSearchService.indexProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product indexed", null));
    }

    @PostMapping("/products")
    @Operation(summary = "Index multiple products (bulk)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> indexProducts(@Valid @RequestBody List<IndexProductRequest> requests) {
        productSearchService.indexProducts(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(requests.size() + " products indexed", null));
    }

    @DeleteMapping("/product/{productId}")
    @Operation(summary = "Delete a product from index", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productSearchService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from index", null));
    }

    @DeleteMapping("/products")
    @Operation(summary = "Delete multiple products from index", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> deleteProducts(@RequestBody List<Long> productIds) {
        productSearchService.deleteProducts(productIds);
        return ResponseEntity.ok(ApiResponse.success(productIds.size() + " products removed from index", null));
    }
}
