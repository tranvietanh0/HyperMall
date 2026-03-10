package com.hypermall.notification.event;

import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements Serializable {

    private Long userId;
    private NotificationType type;
    private List<NotificationChannel> channels;
    private String title;
    private String content;
    private Map<String, Object> data;
    private String referenceType;
    private Long referenceId;
    private String recipientEmail;
    private String recipientPhone;
}
