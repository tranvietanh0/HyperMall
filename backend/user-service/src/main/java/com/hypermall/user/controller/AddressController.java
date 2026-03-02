package com.hypermall.user.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.user.dto.AddressRequest;
import com.hypermall.user.dto.AddressResponse;
import com.hypermall.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "User address management APIs")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get all addresses of current user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @CurrentUser UserPrincipal currentUser) {
        List<AddressResponse> addresses = addressService.getAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        AddressResponse address = addressService.getAddress(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PostMapping
    @Operation(summary = "Create new address")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.createAddress(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.updateAddress(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        addressService.deleteAddress(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Address deleted successfully"));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        AddressResponse address = addressService.setDefaultAddress(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Default address set successfully", address));
    }
}
