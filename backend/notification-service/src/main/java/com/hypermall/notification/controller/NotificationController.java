package com.hypermall.notification.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.notification.dto.*;
import com.hypermall.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {

        PageResponse<NotificationResponse> notifications =
                notificationService.getUserNotifications(currentUser.getId(), page, size, category);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {

        NotificationResponse notification = notificationService.getNotificationById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {

        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @CurrentUser UserPrincipal currentUser) {

        UnreadCountResponse count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> getPreferences(
            @CurrentUser UserPrincipal currentUser) {

        List<NotificationPreferenceResponse> preferences =
                notificationService.getUserPreferences(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preference")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreference(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody NotificationPreferenceRequest request) {

        NotificationPreferenceResponse preference =
                notificationService.updatePreference(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Preference updated successfully", preference));
    }

    @PostMapping("/devices")
    @Operation(summary = "Register device token for push notifications")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody DeviceTokenRequest request) {

        notificationService.registerDeviceToken(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Device registered successfully"));
    }

    @DeleteMapping("/devices/{token}")
    @Operation(summary = "Unregister device token")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String token) {

        notificationService.unregisterDeviceToken(currentUser.getId(), token);
        return ResponseEntity.ok(ApiResponse.success("Device unregistered successfully"));
    }
}
