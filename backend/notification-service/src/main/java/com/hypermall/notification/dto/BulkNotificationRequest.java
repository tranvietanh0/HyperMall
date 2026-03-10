package com.hypermall.notification.dto;

import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkNotificationRequest {

    @NotEmpty(message = "User IDs are required")
    private List<Long> userIds;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private List<NotificationChannel> channels;

    private String title;

    private String content;

    private Map<String, Object> data;
}
