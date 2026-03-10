package com.hypermall.notification.dto;

import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationStatus;
import com.hypermall.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String title;
    private String content;
    private Map<String, Object> data;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
