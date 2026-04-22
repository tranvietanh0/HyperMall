package com.hypermall.seller.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.seller.dto.response.InternalSellerStatusResponse;
import com.hypermall.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/sellers")
@RequiredArgsConstructor
@Tag(name = "Internal Seller", description = "Internal seller status APIs")
public class InternalSellerController {

    private final SellerService sellerService;

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get seller status by user id for internal services")
    public ResponseEntity<ApiResponse<InternalSellerStatusResponse>> getSellerByUserId(@PathVariable Long userId) {
        InternalSellerStatusResponse response = sellerService.getInternalSellerStatusByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
