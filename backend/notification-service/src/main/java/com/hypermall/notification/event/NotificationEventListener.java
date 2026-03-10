package com.hypermall.notification.event;

import com.hypermall.notification.dto.SendNotificationRequest;
import com.hypermall.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.notification:notification-queue}")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: type={}, userId={}", event.getType(), event.getUserId());

        try {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .userId(event.getUserId())
                    .type(event.getType())
                    .channels(event.getChannels())
                    .title(event.getTitle())
                    .content(event.getContent())
                    .data(event.getData())
                    .referenceType(event.getReferenceType())
                    .referenceId(event.getReferenceId())
                    .sendImmediately(true)
                    .build();

            notificationService.sendNotification(request, event.getRecipientEmail(), event.getRecipientPhone());
            log.info("Notification sent successfully for user: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
        }
    }
}
