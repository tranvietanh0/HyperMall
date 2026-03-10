package com.hypermall.shipping.dto.response;

import com.hypermall.shipping.entity.ShipmentStatus;
import com.hypermall.shipping.entity.ShippingProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private Long orderId;
    private Long sellerId;
    private ShippingProvider provider;
    private String providerName;
    private String trackingNumber;
    private String providerOrderCode;
    private ShipmentStatus status;

    private String senderName;
    private String senderPhone;
    private String senderAddress;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private Integer weight;
    private BigDecimal codAmount;
    private BigDecimal shippingFee;
    private BigDecimal insuranceFee;
    private String note;

    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime createdAt;
}
