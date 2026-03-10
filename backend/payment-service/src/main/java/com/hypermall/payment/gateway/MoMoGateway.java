package com.hypermall.payment.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.payment.properties.MoMoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MoMoGateway {

    private final MoMoProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String createPaymentUrl(Long orderId, String orderNumber, BigDecimal amount) {
        String requestId = UUID.randomUUID().toString();
        String requestType = "payWithMethod";
        String extraData = "";
        long amountLong = amount.longValue();

        String rawSignature = "accessKey=" + properties.getAccessKey()
                + "&amount=" + amountLong
                + "&extraData=" + extraData
                + "&ipnUrl=" + properties.getNotifyUrl()
                + "&orderId=" + orderNumber
                + "&orderInfo=Thanh toan don hang " + orderNumber
                + "&partnerCode=" + properties.getPartnerCode()
                + "&redirectUrl=" + properties.getReturnUrl()
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = hmacSHA256(properties.getSecretKey(), rawSignature);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", properties.getPartnerCode());
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amountLong);
        requestBody.put("orderId", orderNumber);
        requestBody.put("orderInfo", "Thanh toan don hang " + orderNumber);
        requestBody.put("redirectUrl", properties.getReturnUrl());
        requestBody.put("ipnUrl", properties.getNotifyUrl());
        requestBody.put("lang", "vi");
        requestBody.put("requestType", requestType);
        requestBody.put("autoCapture", true);
        requestBody.put("extraData", extraData);
        requestBody.put("signature", signature);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    properties.getApiUrl() + "/v2/gateway/api/create",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            Map<?, ?> response = objectMapper.readValue(responseEntity.getBody(), Map.class);
            int resultCode = (int) response.get("resultCode");

            if (resultCode == 0) {
                String payUrl = (String) response.get("payUrl");
                log.debug("MoMo payment URL created for order: {}", orderNumber);
                return payUrl;
            } else {
                log.error("MoMo payment creation failed for order {}: {}", orderNumber, response.get("message"));
                throw new RuntimeException("MoMo payment creation failed: " + response.get("message"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MoMo payment: " + e.getMessage(), e);
        }
    }

    public boolean verifyCallback(Map<String, String> params) {
        String receivedSignature = params.get("signature");
        if (receivedSignature == null) return false;

        String rawSignature = "accessKey=" + properties.getAccessKey()
                + "&amount=" + params.get("amount")
                + "&extraData=" + params.getOrDefault("extraData", "")
                + "&message=" + params.getOrDefault("message", "")
                + "&orderId=" + params.get("orderId")
                + "&orderInfo=" + params.getOrDefault("orderInfo", "")
                + "&orderType=" + params.getOrDefault("orderType", "")
                + "&partnerCode=" + params.get("partnerCode")
                + "&payType=" + params.getOrDefault("payType", "")
                + "&requestId=" + params.get("requestId")
                + "&responseTime=" + params.getOrDefault("responseTime", "")
                + "&resultCode=" + params.get("resultCode")
                + "&transId=" + params.get("transId");

        String expectedSignature = hmacSHA256(properties.getSecretKey(), rawSignature);
        return expectedSignature.equals(receivedSignature);
    }

    public boolean isPaymentSuccess(Map<String, String> params) {
        return "0".equals(params.get("resultCode"));
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
