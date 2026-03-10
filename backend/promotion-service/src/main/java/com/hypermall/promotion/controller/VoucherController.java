package com.hypermall.promotion.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.promotion.dto.*;
import com.hypermall.promotion.entity.VoucherStatus;
import com.hypermall.promotion.service.VoucherService;
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
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers", description = "Voucher and discount code management")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    @Operation(summary = "Create a new voucher (Admin/Seller)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse voucher = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Voucher created", voucher));
    }

    @GetMapping
    @Operation(summary = "Get all vouchers (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<VoucherResponse>>> getVouchers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<VoucherResponse> page = voucherService.getVouchers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get voucher by ID")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.getVoucherById(id);
        return ResponseEntity.ok(ApiResponse.success(voucher));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get voucher by code")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByCode(@PathVariable String code) {
        VoucherResponse voucher = voucherService.getVoucherByCode(code);
        return ResponseEntity.ok(ApiResponse.success(voucher));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available vouchers for user")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAvailableVouchers(
            @RequestParam(required = false) Long sellerId) {
        List<VoucherResponse> vouchers = voucherService.getAvailableVouchers(sellerId);
        return ResponseEntity.ok(ApiResponse.success(vouchers));
    }

    @GetMapping("/my-vouchers")
    @Operation(summary = "Get user's claimed vouchers", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getMyVouchers(
            @CurrentUser UserPrincipal currentUser) {
        List<VoucherResponse> vouchers = voucherService.getUserVouchers(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(vouchers));
    }

    @PostMapping("/claim/{code}")
    @Operation(summary = "Claim a voucher", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<VoucherResponse>> claimVoucher(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String code) {
        VoucherResponse voucher = voucherService.claimVoucher(currentUser.getId(), code);
        return ResponseEntity.ok(ApiResponse.success("Voucher claimed successfully", voucher));
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply voucher to order", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ApplyVoucherResponse>> applyVoucher(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ApplyVoucherRequest request) {
        ApplyVoucherResponse result = voucherService.applyVoucher(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update voucher status (Admin)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucherStatus(
            @PathVariable Long id,
            @RequestParam VoucherStatus status) {
        VoucherResponse voucher = voucherService.updateVoucherStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Voucher status updated", voucher));
    }
}
