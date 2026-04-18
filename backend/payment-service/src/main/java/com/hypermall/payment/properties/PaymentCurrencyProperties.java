package com.hypermall.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.payment.currency")
@Data
public class PaymentCurrencyProperties {
    private String displayCurrency = "USD";
    private String settlementCurrency = "VND";
    private BigDecimal usdToVndRate = BigDecimal.valueOf(25000);
}
