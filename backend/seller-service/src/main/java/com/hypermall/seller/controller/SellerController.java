package com.hypermall.seller.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.seller.dto.request.CreateSellerRequest;
import com.hypermall.seller.dto.request.UpdateSellerRequest;
import com.hypermall.seller.dto.response.SellerResponse;
import com.hypermall.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller", description = "Seller profile APIs")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/register")
    @Operation(summary = "Register current user as seller")
    public ResponseEntity<ApiResponse<SellerResponse>> register(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateSellerRequest request) {
        SellerResponse response = sellerService.registerSeller(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seller registration submitted", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current seller profile")
    public ResponseEntity<ApiResponse<SellerResponse>> getMyProfile(@CurrentUser UserPrincipal currentUser) {
        SellerResponse response = sellerService.getMySellerProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current seller profile")
    public ResponseEntity<ApiResponse<SellerResponse>> updateMyProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateSellerRequest request) {
        SellerResponse response = sellerService.updateMySellerProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Seller profile updated", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get seller by id")
    public ResponseEntity<ApiResponse<SellerResponse>> getSellerById(@PathVariable Long id) {
        SellerResponse response = sellerService.getSellerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get seller by shop slug")
    public ResponseEntity<ApiResponse<SellerResponse>> getSellerBySlug(@PathVariable String slug) {
        SellerResponse response = sellerService.getSellerBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
