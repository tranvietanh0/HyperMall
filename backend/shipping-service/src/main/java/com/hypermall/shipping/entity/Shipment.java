package com.hypermall.shipping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_tracking_number", columnList = "tracking_number"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShippingProvider provider;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "provider_order_code", length = 100)
    private String providerOrderCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PENDING;

    // Sender info
    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "sender_phone", nullable = false, length = 20)
    private String senderPhone;

    @Column(name = "sender_address", nullable = false, length = 500)
    private String senderAddress;

    @Column(name = "sender_province", length = 100)
    private String senderProvince;

    @Column(name = "sender_district", length = 100)
    private String senderDistrict;

    @Column(name = "sender_ward", length = 100)
    private String senderWard;

    // Receiver info
    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", nullable = false, length = 500)
    private String receiverAddress;

    @Column(name = "receiver_province", length = 100)
    private String receiverProvince;

    @Column(name = "receiver_district", length = 100)
    private String receiverDistrict;

    @Column(name = "receiver_ward", length = 100)
    private String receiverWard;

    // Package info
    @Column(nullable = false)
    private Integer weight; // gram

    @Column
    private Integer length; // cm

    @Column
    private Integer width; // cm

    @Column
    private Integer height; // cm

    @Column(name = "cod_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "insurance_fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal insuranceFee = BigDecimal.ZERO;

    @Column(length = 500)
    private String note;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
