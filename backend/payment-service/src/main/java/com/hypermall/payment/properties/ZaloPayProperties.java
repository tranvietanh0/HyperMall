package com.hypermall.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.payment.zalopay")
@Data
public class ZaloPayProperties {
    private String appId;
    private String key1;
    private String key2;
    private String endpoint;
    private String callbackUrl;
}
