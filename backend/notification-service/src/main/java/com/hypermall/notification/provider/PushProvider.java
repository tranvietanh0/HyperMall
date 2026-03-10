package com.hypermall.notification.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.notification.entity.DeviceToken;
import com.hypermall.notification.entity.Notification;
import com.hypermall.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushProvider implements NotificationProvider {

    private final DeviceTokenRepository deviceTokenRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${notification.push.enabled:false}")
    private boolean pushEnabled;

    @Value("${notification.push.firebase.server-key:}")
    private String firebaseServerKey;

    @Value("${notification.push.firebase.api-url:https://fcm.googleapis.com/fcm/send}")
    private String firebaseApiUrl;

    @Override
    public boolean send(Notification notification) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(notification.getUserId());

        if (tokens.isEmpty()) {
            log.info("No active device tokens found for user: {}", notification.getUserId());
            return true;
        }

        if (!pushEnabled) {
            log.info("Push notifications disabled. Would send to {} devices for user: {}",
                    tokens.size(), notification.getUserId());
            return true;
        }

        boolean allSuccess = true;
        for (DeviceToken token : tokens) {
            boolean success = sendToDevice(notification, token.getToken());
            if (!success) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    private boolean sendToDevice(Notification notification, String deviceToken) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("to", deviceToken);

            Map<String, String> notificationPayload = new HashMap<>();
            notificationPayload.put("title", notification.getTitle());
            notificationPayload.put("body", notification.getContent());
            notificationPayload.put("click_action", "OPEN_NOTIFICATION");
            message.put("notification", notificationPayload);

            Map<String, Object> data = new HashMap<>();
            data.put("type", notification.getType().name());
            data.put("notificationId", notification.getId().toString());
            if (notification.getReferenceType() != null) {
                data.put("referenceType", notification.getReferenceType());
            }
            if (notification.getReferenceId() != null) {
                data.put("referenceId", notification.getReferenceId().toString());
            }

            if (notification.getData() != null) {
                try {
                    Map<String, Object> additionalData = objectMapper.readValue(
                            notification.getData(), new TypeReference<Map<String, Object>>() {});
                    data.putAll(additionalData);
                } catch (Exception e) {
                    log.warn("Failed to parse notification data: {}", e.getMessage());
                }
            }
            message.put("data", data);

            // Mock implementation - replace with actual Firebase API call
            log.info("Sending push notification to device: {}...", deviceToken.substring(0, Math.min(20, deviceToken.length())));

            // Actual implementation would be:
            // webClientBuilder.build()
            //     .post()
            //     .uri(firebaseApiUrl)
            //     .header("Authorization", "key=" + firebaseServerKey)
            //     .header("Content-Type", "application/json")
            //     .bodyValue(message)
            //     .retrieve()
            //     .bodyToMono(String.class)
            //     .block();

            return true;

        } catch (Exception e) {
            log.error("Failed to send push notification: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "PUSH";
    }
}
