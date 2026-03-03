package com.hypermall.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.payment.momo")
@Data
public class MoMoProperties {
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String apiUrl;
    private String returnUrl;
    private String notifyUrl;
}
