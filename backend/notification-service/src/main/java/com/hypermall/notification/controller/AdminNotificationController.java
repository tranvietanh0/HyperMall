package com.hypermall.notification.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.notification.dto.BulkNotificationRequest;
import com.hypermall.notification.dto.NotificationResponse;
import com.hypermall.notification.dto.SendNotificationRequest;
import com.hypermall.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin Notifications", description = "Admin notification management APIs")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send notification to a user (Admin only)")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        request.setSendImmediately(true);
        NotificationResponse notification = notificationService.sendNotification(request, email, phone);
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", notification));
    }

    @PostMapping("/send-bulk")
    @Operation(summary = "Send notification to multiple users (Admin only)")
    public ResponseEntity<ApiResponse<Void>> sendBulkNotification(
            @Valid @RequestBody BulkNotificationRequest request) {

        notificationService.sendBulkNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Bulk notifications sent successfully"));
    }
}
