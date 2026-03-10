package com.hypermall.notification.dto;

import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceResponse {

    private Long id;
    private NotificationType type;
    private NotificationChannel channel;
    private Boolean enabled;
}
