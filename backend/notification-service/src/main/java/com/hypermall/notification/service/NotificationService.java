package com.hypermall.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.notification.dto.*;
import com.hypermall.notification.entity.*;
import com.hypermall.notification.mapper.NotificationMapper;
import com.hypermall.notification.provider.EmailProvider;
import com.hypermall.notification.provider.PushProvider;
import com.hypermall.notification.provider.SmsProvider;
import com.hypermall.notification.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationMapper notificationMapper;
    private final EmailProvider emailProvider;
    private final SmsProvider smsProvider;
    private final PushProvider pushProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request, String recipientEmail, String recipientPhone) {
        List<NotificationChannel> channels = request.getChannels();
        if (channels == null || channels.isEmpty()) {
            channels = getDefaultChannels(request.getType());
        }

        List<Notification> notifications = new ArrayList<>();

        for (NotificationChannel channel : channels) {
            if (!isChannelEnabled(request.getUserId(), request.getType(), channel)) {
                log.debug("Channel {} disabled for user {} and type {}", channel, request.getUserId(), request.getType());
                continue;
            }

            String title = request.getTitle();
            String content = request.getContent();

            if (title == null || content == null) {
                NotificationTemplate template = templateRepository
                        .findByTypeAndChannelAndIsActiveTrue(request.getType(), channel)
                        .orElse(null);

                if (template != null) {
                    title = title != null ? title : processTemplate(template.getTitleTemplate(), request.getData());
                    content = content != null ? content : processTemplate(template.getContentTemplate(), request.getData());
                } else {
                    title = title != null ? title : getDefaultTitle(request.getType());
                    content = content != null ? content : getDefaultContent(request.getType());
                }
            }

            String recipient = switch (channel) {
                case EMAIL -> recipientEmail;
                case SMS -> recipientPhone;
                default -> null;
            };

            Notification notification = Notification.builder()
                    .userId(request.getUserId())
                    .type(request.getType())
                    .channel(channel)
                    .status(NotificationStatus.PENDING)
                    .title(title)
                    .content(content)
                    .data(toJson(request.getData()))
                    .recipient(recipient)
                    .referenceType(request.getReferenceType())
                    .referenceId(request.getReferenceId())
                    .build();

            notification = notificationRepository.save(notification);
            notifications.add(notification);

            if (Boolean.TRUE.equals(request.getSendImmediately())) {
                sendAsync(notification);
            }
        }

        return notifications.isEmpty() ? null : notificationMapper.toResponse(notifications.get(0));
    }

    @Async
    public void sendAsync(Notification notification) {
        boolean success = switch (notification.getChannel()) {
            case EMAIL -> emailProvider.send(notification);
            case SMS -> smsProvider.send(notification);
            case PUSH -> pushProvider.send(notification);
            case IN_APP -> true;
        };

        notification.setStatus(success ? NotificationStatus.SENT : NotificationStatus.FAILED);
        notification.setSentAt(success ? LocalDateTime.now() : null);
        if (!success) {
            notification.setRetryCount(notification.getRetryCount() + 1);
        }
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendBulkNotification(BulkNotificationRequest request) {
        for (Long userId : request.getUserIds()) {
            SendNotificationRequest singleRequest = SendNotificationRequest.builder()
                    .userId(userId)
                    .type(request.getType())
                    .channels(request.getChannels())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .data(request.getData())
                    .sendImmediately(true)
                    .build();

            sendNotification(singleRequest, null, null);
        }
    }

    public PageResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications;

        if (category != null && !category.isEmpty()) {
            List<NotificationType> types = getCategoryTypes(category);
            notifications = notificationRepository.findByUserIdAndChannelAndTypeInOrderByCreatedAtDesc(
                    userId, NotificationChannel.IN_APP, types, pageable);
        } else {
            notifications = notificationRepository.findByUserIdAndChannelOrderByCreatedAtDesc(
                    userId, NotificationChannel.IN_APP, pageable);
        }

        List<NotificationResponse> content = notifications.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .build();
    }

    public NotificationResponse getNotificationById(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        int updated = notificationRepository.markAsRead(id, userId, LocalDateTime.now());
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, NotificationChannel.IN_APP, LocalDateTime.now());
    }

    public UnreadCountResponse getUnreadCount(Long userId) {
        Long total = notificationRepository.countUnreadByUserIdAndChannel(userId, NotificationChannel.IN_APP);

        List<NotificationType> orderTypes = Arrays.asList(
                NotificationType.ORDER_CREATED, NotificationType.ORDER_CONFIRMED,
                NotificationType.ORDER_PROCESSING, NotificationType.ORDER_SHIPPED,
                NotificationType.ORDER_DELIVERED, NotificationType.ORDER_CANCELLED
        );
        Long orders = notificationRepository.countUnreadByUserIdAndChannelAndTypes(
                userId, NotificationChannel.IN_APP, orderTypes);

        List<NotificationType> promoTypes = Arrays.asList(
                NotificationType.PROMOTION, NotificationType.FLASH_SALE,
                NotificationType.PRICE_DROP, NotificationType.VOUCHER_EXPIRING
        );
        Long promotions = notificationRepository.countUnreadByUserIdAndChannelAndTypes(
                userId, NotificationChannel.IN_APP, promoTypes);

        return UnreadCountResponse.builder()
                .total(total)
                .orders(orders)
                .promotions(promotions)
                .system(total - orders - promotions)
                .build();
    }

    public List<NotificationPreferenceResponse> getUserPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId).stream()
                .map(notificationMapper::toPreferenceResponse)
                .toList();
    }

    @Transactional
    public NotificationPreferenceResponse updatePreference(Long userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository
                .findByUserIdAndTypeAndChannel(userId, request.getType(), request.getChannel())
                .orElse(NotificationPreference.builder()
                        .userId(userId)
                        .type(request.getType())
                        .channel(request.getChannel())
                        .build());

        preference.setEnabled(request.getEnabled());
        preference = preferenceRepository.save(preference);

        return notificationMapper.toPreferenceResponse(preference);
    }

    @Transactional
    public void registerDeviceToken(Long userId, DeviceTokenRequest request) {
        DeviceToken existingToken = deviceTokenRepository.findByToken(request.getToken()).orElse(null);

        if (existingToken != null) {
            existingToken.setUserId(userId);
            existingToken.setIsActive(true);
            existingToken.setLastUsedAt(LocalDateTime.now());
            deviceTokenRepository.save(existingToken);
        } else {
            DeviceToken token = DeviceToken.builder()
                    .userId(userId)
                    .token(request.getToken())
                    .deviceType(request.getDeviceType())
                    .deviceName(request.getDeviceName())
                    .isActive(true)
                    .lastUsedAt(LocalDateTime.now())
                    .build();
            deviceTokenRepository.save(token);
        }
    }

    @Transactional
    public void unregisterDeviceToken(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    private boolean isChannelEnabled(Long userId, NotificationType type, NotificationChannel channel) {
        return preferenceRepository.findByUserIdAndTypeAndChannel(userId, type, channel)
                .map(NotificationPreference::getEnabled)
                .orElse(true);
    }

    private List<NotificationChannel> getDefaultChannels(NotificationType type) {
        return switch (type) {
            case ORDER_CREATED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED ->
                    Arrays.asList(NotificationChannel.IN_APP, NotificationChannel.EMAIL, NotificationChannel.PUSH);
            case PAYMENT_SUCCESS, PAYMENT_FAILED, REFUND_PROCESSED ->
                    Arrays.asList(NotificationChannel.IN_APP, NotificationChannel.EMAIL);
            case PROMOTION, FLASH_SALE, PRICE_DROP ->
                    Arrays.asList(NotificationChannel.IN_APP, NotificationChannel.PUSH);
            case PASSWORD_RESET, EMAIL_VERIFICATION ->
                    List.of(NotificationChannel.EMAIL);
            default -> List.of(NotificationChannel.IN_APP);
        };
    }

    private List<NotificationType> getCategoryTypes(String category) {
        return switch (category.toLowerCase()) {
            case "orders" -> Arrays.asList(
                    NotificationType.ORDER_CREATED, NotificationType.ORDER_CONFIRMED,
                    NotificationType.ORDER_PROCESSING, NotificationType.ORDER_SHIPPED,
                    NotificationType.ORDER_DELIVERED, NotificationType.ORDER_CANCELLED,
                    NotificationType.ORDER_RETURNED
            );
            case "promotions" -> Arrays.asList(
                    NotificationType.PROMOTION, NotificationType.FLASH_SALE,
                    NotificationType.PRICE_DROP, NotificationType.VOUCHER_EXPIRING
            );
            case "system" -> Arrays.asList(
                    NotificationType.SYSTEM_ANNOUNCEMENT, NotificationType.MAINTENANCE,
                    NotificationType.WELCOME
            );
            default -> Arrays.asList(NotificationType.values());
        };
    }

    private String processTemplate(String template, Map<String, Object> data) {
        if (template == null || data == null) return template;
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    private String getDefaultTitle(NotificationType type) {
        return switch (type) {
            case ORDER_CREATED -> "Order Placed Successfully";
            case ORDER_CONFIRMED -> "Order Confirmed";
            case ORDER_SHIPPED -> "Order Shipped";
            case ORDER_DELIVERED -> "Order Delivered";
            case ORDER_CANCELLED -> "Order Cancelled";
            case PAYMENT_SUCCESS -> "Payment Successful";
            case PAYMENT_FAILED -> "Payment Failed";
            case PROMOTION -> "New Promotion Available";
            case FLASH_SALE -> "Flash Sale Starting Soon";
            case WELCOME -> "Welcome to HyperMall";
            default -> "Notification from HyperMall";
        };
    }

    private String getDefaultContent(NotificationType type) {
        return switch (type) {
            case ORDER_CREATED -> "Your order has been placed successfully.";
            case ORDER_CONFIRMED -> "Your order has been confirmed by the seller.";
            case ORDER_SHIPPED -> "Your order is on the way!";
            case ORDER_DELIVERED -> "Your order has been delivered.";
            case ORDER_CANCELLED -> "Your order has been cancelled.";
            case PAYMENT_SUCCESS -> "Your payment was successful.";
            case PAYMENT_FAILED -> "Your payment failed. Please try again.";
            case WELCOME -> "Welcome to HyperMall! Start shopping now.";
            default -> "You have a new notification.";
        };
    }

    private String toJson(Map<String, Object> data) {
        if (data == null) return null;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert data to JSON: {}", e.getMessage());
            return null;
        }
    }
}
