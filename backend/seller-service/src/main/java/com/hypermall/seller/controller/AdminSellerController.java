package com.hypermall.seller.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.seller.dto.response.SellerResponse;
import com.hypermall.seller.entity.SellerStatus;
import com.hypermall.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
@Tag(name = "Admin Seller", description = "Admin seller approval APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final SellerService sellerService;

    @GetMapping
    @Operation(summary = "List sellers by status")
    public ResponseEntity<ApiResponse<List<SellerResponse>>> getSellers(
            @RequestParam(defaultValue = "PENDING") SellerStatus status) {
        List<SellerResponse> response = sellerService.getSellersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update seller status")
    public ResponseEntity<ApiResponse<SellerResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam SellerStatus status) {
        SellerResponse response = sellerService.updateSellerStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Seller status updated", response));
    }
}
