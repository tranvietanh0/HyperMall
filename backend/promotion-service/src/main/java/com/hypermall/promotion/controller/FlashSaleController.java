package com.hypermall.promotion.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.promotion.dto.*;
import com.hypermall.promotion.entity.FlashSaleStatus;
import com.hypermall.promotion.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
@Tag(name = "Flash Sales", description = "Flash sale and limited-time deal management")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PostMapping
    @Operation(summary = "Create a new flash sale (Admin)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<FlashSaleResponse>> createFlashSale(
            @Valid @RequestBody CreateFlashSaleRequest request) {
        FlashSaleResponse flashSale = flashSaleService.createFlashSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flash sale created", flashSale));
    }

    @GetMapping
    @Operation(summary = "Get all flash sales")
    public ResponseEntity<ApiResponse<PageResponse<FlashSaleResponse>>> getFlashSales(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FlashSaleResponse> page = flashSaleService.getFlashSales(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flash sale by ID")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> getFlashSaleById(@PathVariable Long id) {
        FlashSaleResponse flashSale = flashSaleService.getFlashSaleById(id);
        return ResponseEntity.ok(ApiResponse.success(flashSale));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current active flash sale")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> getCurrentFlashSale() {
        return flashSaleService.getCurrentFlashSale()
                .map(fs -> ResponseEntity.ok(ApiResponse.success(fs)))
                .orElse(ResponseEntity.ok(ApiResponse.success("No active flash sale", null)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active flash sales")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getActiveFlashSales() {
        List<FlashSaleResponse> flashSales = flashSaleService.getActiveFlashSales();
        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming flash sales")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getUpcomingFlashSales() {
        List<FlashSaleResponse> flashSales = flashSaleService.getUpcomingFlashSales();
        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Check if product is in active flash sale")
    public ResponseEntity<ApiResponse<FlashSaleProductResponse>> getFlashSaleProduct(@PathVariable Long productId) {
        return flashSaleService.getActiveFlashSaleProduct(productId)
                .map(fsp -> ResponseEntity.ok(ApiResponse.success(fsp)))
                .orElse(ResponseEntity.ok(ApiResponse.success("Product not in flash sale", null)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update flash sale status (Admin)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<FlashSaleResponse>> updateFlashSaleStatus(
            @PathVariable Long id,
            @RequestParam FlashSaleStatus status) {
        FlashSaleResponse flashSale = flashSaleService.updateFlashSaleStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Flash sale status updated", flashSale));
    }
}
