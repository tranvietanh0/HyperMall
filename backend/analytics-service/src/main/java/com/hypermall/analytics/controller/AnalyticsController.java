package com.hypermall.analytics.controller;

import com.hypermall.analytics.dto.request.TrackEventRequest;
import com.hypermall.analytics.dto.response.DashboardStatsResponse;
import com.hypermall.analytics.service.AnalyticsService;
import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics tracking and reporting APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/track")
    @Operation(summary = "Track an event")
    public ResponseEntity<ApiResponse<Void>> trackEvent(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TrackEventRequest request,
            HttpServletRequest httpRequest) {

        Long userId = currentUser != null ? currentUser.getId() : null;
        analyticsService.trackEvent(userId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Event tracked", null));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(
            @RequestParam(required = false) Long sellerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardStatsResponse response = analyticsService.getDashboardStats(sellerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/seller/dashboard")
    @Operation(summary = "Get seller dashboard stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getSellerDashboardStats(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        DashboardStatsResponse response = analyticsService.getDashboardStats(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search/trending")
    @Operation(summary = "Get trending search queries")
    public ResponseEntity<ApiResponse<List<String>>> getTrendingSearches(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<String> queries = analyticsService.getTopSearchQueries(limit, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(queries));
    }
}
