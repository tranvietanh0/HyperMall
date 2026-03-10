package com.hypermall.shipping.dto.response;

import com.hypermall.shipping.entity.ShipmentStatus;
import com.hypermall.shipping.entity.ShippingProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {

    private String trackingNumber;
    private ShippingProvider provider;
    private String providerName;
    private ShipmentStatus currentStatus;
    private String receiverName;
    private String receiverAddress;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime deliveredAt;
    private List<TrackingEventResponse> events;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEventResponse {
        private ShipmentStatus status;
        private String description;
        private String location;
        private LocalDateTime eventTime;
    }
}
