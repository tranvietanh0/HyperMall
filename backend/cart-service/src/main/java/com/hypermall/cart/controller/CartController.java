package com.hypermall.cart.controller;

import com.hypermall.cart.dto.*;
import com.hypermall.cart.service.CartService;
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
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@CurrentUser UserPrincipal currentUser) {
        CartResponse cart = cartService.getCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity or selection")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateItem(currentUser.getId(), itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String itemId) {
        CartResponse cart = cartService.removeItem(currentUser.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all items from cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    @PostMapping("/checkout-preview")
    @Operation(summary = "Preview checkout with selected items")
    public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> checkoutPreview(
            @CurrentUser UserPrincipal currentUser) {
        CheckoutPreviewResponse preview = cartService.getCheckoutPreview(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    @PutMapping("/select-all")
    @Operation(summary = "Select or deselect all cart items")
    public ResponseEntity<ApiResponse<CartResponse>> selectAll(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "true") boolean selected) {
        CartResponse cart = cartService.selectAll(currentUser.getId(), selected);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
}
