package com.hypermall.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.payment.vnpay")
@Data
public class VNPayProperties {
    private String tmnCode;
    private String hashSecret;
    private String url;
    private String returnUrl;
}
