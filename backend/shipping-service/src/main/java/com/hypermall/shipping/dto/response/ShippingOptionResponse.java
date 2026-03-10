package com.hypermall.shipping.dto.response;

import com.hypermall.shipping.entity.ShippingProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOptionResponse {

    private ShippingProvider provider;
    private String providerName;
    private String serviceName;
    private BigDecimal shippingFee;
    private BigDecimal insuranceFee;
    private BigDecimal totalFee;
    private Integer estimatedDays;
    private String estimatedDelivery;
}
