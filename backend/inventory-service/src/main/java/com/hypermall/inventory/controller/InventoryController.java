package com.hypermall.inventory.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.inventory.dto.request.CreateInventoryRequest;
import com.hypermall.inventory.dto.request.ReserveStockRequest;
import com.hypermall.inventory.dto.request.UpdateStockRequest;
import com.hypermall.inventory.dto.response.InventoryResponse;
import com.hypermall.inventory.dto.response.StockCheckResponse;
import com.hypermall.inventory.dto.response.StockMovementResponse;
import com.hypermall.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create inventory for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse inventory = inventoryService.createInventory(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory created successfully", inventory));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoriesByProduct(
            @PathVariable Long productId) {
        List<InventoryResponse> inventories = inventoryService.getInventoriesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(inventories));
    }

    @GetMapping("/product/{productId}/variant/{variantId}")
    @Operation(summary = "Get inventory by product and variant")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        InventoryResponse inventory = inventoryService.getInventory(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @GetMapping("/seller")
    @Operation(summary = "Get seller's inventories")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getSellerInventories(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        Page<InventoryResponse> inventories = inventoryService.getSellerInventories(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(inventories)));
    }

    @GetMapping("/seller/low-stock")
    @Operation(summary = "Get seller's low stock inventories")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getLowStockInventories(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        Page<InventoryResponse> inventories = inventoryService.getLowStockInventories(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(inventories)));
    }

    @GetMapping("/seller/out-of-stock")
    @Operation(summary = "Get seller's out of stock inventories")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getOutOfStockInventories(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        Page<InventoryResponse> inventories = inventoryService.getOutOfStockInventories(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(inventories)));
    }

    @PutMapping("/{inventoryId}")
    @Operation(summary = "Update stock (add/remove/adjust)")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateStockRequest request) {
        InventoryResponse inventory = inventoryService.updateStock(currentUser.getId(), inventoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", inventory));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public ResponseEntity<ApiResponse<Void>> reserveStock(
            @Valid @RequestBody ReserveStockRequest request) {
        inventoryService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.<Void>success("Stock reserved successfully"));
    }

    @PostMapping("/release/{orderId}")
    @Operation(summary = "Release reserved stock (order cancelled)")
    public ResponseEntity<ApiResponse<Void>> releaseStock(@PathVariable Long orderId) {
        inventoryService.releaseStock(orderId);
        return ResponseEntity.ok(ApiResponse.<Void>success("Stock released successfully"));
    }

    @PostMapping("/confirm/{orderId}")
    @Operation(summary = "Confirm stock deduction (order completed)")
    public ResponseEntity<ApiResponse<Void>> confirmStock(@PathVariable Long orderId) {
        inventoryService.confirmStock(orderId);
        return ResponseEntity.ok(ApiResponse.<Void>success("Stock confirmed successfully"));
    }

    @PostMapping("/check")
    @Operation(summary = "Check if items are in stock")
    public ResponseEntity<ApiResponse<StockCheckResponse>> checkStock(
            @Valid @RequestBody List<ReserveStockRequest.ReserveItem> items) {
        StockCheckResponse response = inventoryService.checkStock(items);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{inventoryId}/movements")
    @Operation(summary = "Get stock movements history")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementResponse>>> getMovements(
            @PathVariable Long inventoryId,
            Pageable pageable) {
        Page<StockMovementResponse> movements = inventoryService.getMovements(inventoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(movements)));
    }
}
