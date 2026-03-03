package com.hypermall.payment.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.payment.properties.ZaloPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ZaloPayGateway {

    private final ZaloPayProperties properties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public String createPaymentUrl(Long orderId, String orderNumber, BigDecimal amount) {
        long appTime = System.currentTimeMillis();
        String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + orderNumber;
        String appUser = "hypermall_user_" + orderId;
        long amountLong = amount.longValue();
        String embedData = "{\"redirecturl\":\"" + "http://localhost:3000/payment/zalopay/callback" + "\"}";
        String item = "[]";
        String description = "Thanh toan don hang " + orderNumber;

        String rawMac = properties.getAppId() + "|" + appTransId + "|" + appUser + "|"
                + amountLong + "|" + appTime + "|" + embedData + "|" + item;
        String mac = hmacSHA256(properties.getKey1(), rawMac);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("app_id", Integer.parseInt(properties.getAppId()));
        requestBody.put("app_user", appUser);
        requestBody.put("app_time", appTime);
        requestBody.put("amount", amountLong);
        requestBody.put("app_trans_id", appTransId);
        requestBody.put("embed_data", embedData);
        requestBody.put("item", item);
        requestBody.put("description", description);
        requestBody.put("bank_code", "zalopayapp");
        requestBody.put("mac", mac);
        requestBody.put("callback_url", properties.getCallbackUrl());

        try {
            String responseBody = webClientBuilder.build()
                    .post()
                    .uri(properties.getEndpoint() + "/create")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<?, ?> response = objectMapper.readValue(responseBody, Map.class);
            int returnCode = (int) response.get("return_code");

            if (returnCode == 1) {
                String orderUrl = (String) response.get("order_url");
                log.debug("ZaloPay payment URL created for order: {}", orderNumber);
                return orderUrl;
            } else {
                log.error("ZaloPay payment creation failed for order {}: {}", orderNumber, response.get("return_message"));
                throw new RuntimeException("ZaloPay payment creation failed: " + response.get("return_message"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ZaloPay payment: " + e.getMessage(), e);
        }
    }

    public boolean verifyCallback(Map<String, Object> data, String mac) {
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            String expectedMac = hmacSHA256(properties.getKey2(), dataJson);
            return expectedMac.equals(mac);
        } catch (Exception e) {
            log.error("Failed to verify ZaloPay callback", e);
            return false;
        }
    }

    public boolean isPaymentSuccess(Map<String, Object> data) {
        return Integer.valueOf(1).equals(data.get("type"));
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC SHA256", e);
        }
    }
}
