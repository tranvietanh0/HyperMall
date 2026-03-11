package com.hypermall.seller.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.seller.dto.request.CreateSellerRequest;
import com.hypermall.seller.dto.request.UpdateSellerRequest;
import com.hypermall.seller.dto.response.FollowResponse;
import com.hypermall.seller.dto.response.SellerDashboardResponse;
import com.hypermall.seller.dto.response.SellerResponse;
import com.hypermall.seller.entity.SellerStatus;
import com.hypermall.seller.service.SellerFollowerService;
import com.hypermall.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller", description = "Seller profile APIs")
public class SellerController {

    private final SellerService sellerService;
    private final SellerFollowerService followerService;

    @GetMapping
    @Operation(summary = "List public sellers")
    public ResponseEntity<ApiResponse<PageResponse<SellerResponse>>> getPublicSellers(
            @RequestParam(defaultValue = "ACTIVE") SellerStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SellerResponse> response = sellerService.searchSellers(status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(response)));
    }

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

    @GetMapping("/me/dashboard")
    @Operation(summary = "Get seller dashboard summary")
    public ResponseEntity<ApiResponse<SellerDashboardResponse>> getMyDashboard(@CurrentUser UserPrincipal currentUser) {
        SellerDashboardResponse response = sellerService.getMyDashboard(currentUser.getId());
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

    @PostMapping("/{id}/follow")
    @Operation(summary = "Follow a seller")
    public ResponseEntity<ApiResponse<FollowResponse>> followSeller(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        FollowResponse response = followerService.followSeller(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Followed successfully", response));
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "Unfollow a seller")
    public ResponseEntity<ApiResponse<FollowResponse>> unfollowSeller(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        FollowResponse response = followerService.unfollowSeller(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Unfollowed successfully", response));
    }

    @GetMapping("/{id}/following")
    @Operation(summary = "Check if current user is following a seller")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        boolean following = followerService.isFollowing(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(following));
    }

    @GetMapping("/me/following")
    @Operation(summary = "Get list of seller IDs that current user is following")
    public ResponseEntity<ApiResponse<PageResponse<Long>>> getMyFollowing(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Long> sellerIds = followerService.getFollowingSellerIds(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(sellerIds)));
    }
}
