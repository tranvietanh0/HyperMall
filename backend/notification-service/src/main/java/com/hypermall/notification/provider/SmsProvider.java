package com.hypermall.notification.provider;

import com.hypermall.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsProvider implements NotificationProvider {

    private final WebClient.Builder webClientBuilder;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.provider:stringee}")
    private String smsProviderType;

    @Value("${notification.sms.api-key:}")
    private String apiKey;

    @Value("${notification.sms.api-secret:}")
    private String apiSecret;

    @Value("${notification.sms.sender:HyperMall}")
    private String sender;

    @Override
    public boolean send(Notification notification) {
        if (!smsEnabled) {
            log.info("SMS notifications disabled. Would send to: {} - Message: {}",
                    notification.getRecipient(), truncateMessage(notification.getContent()));
            return true;
        }

        return switch (smsProviderType.toLowerCase()) {
            case "twilio" -> sendViaTwilio(notification);
            case "stringee" -> sendViaStringee(notification);
            default -> {
                log.warn("Unknown SMS provider: {}", smsProviderType);
                yield false;
            }
        };
    }

    private boolean sendViaTwilio(Notification notification) {
        try {
            log.info("Sending SMS via Twilio to: {}", notification.getRecipient());
            // Mock implementation - replace with actual Twilio API call
            // TwilioRestClient client = new TwilioRestClient(accountSid, authToken);
            // Message.creator(new PhoneNumber(to), new PhoneNumber(from), body).create();
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendViaStringee(Notification notification) {
        try {
            log.info("Sending SMS via Stringee to: {}", notification.getRecipient());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", notification.getRecipient());
            requestBody.put("from", sender);
            requestBody.put("text", notification.getContent());

            // Mock implementation - replace with actual Stringee API call
            // WebClient response = webClientBuilder.build()
            //     .post()
            //     .uri("https://api.stringee.com/v1/sms")
            //     .header("X-STRINGEE-AUTH", apiKey)
            //     .bodyValue(requestBody)
            //     .retrieve()
            //     .bodyToMono(String.class)
            //     .block();

            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS via Stringee: {}", e.getMessage());
            return false;
        }
    }

    private String truncateMessage(String message) {
        if (message == null) return "";
        return message.length() > 160 ? message.substring(0, 157) + "..." : message;
    }

    @Override
    public String getProviderName() {
        return "SMS";
    }
}
