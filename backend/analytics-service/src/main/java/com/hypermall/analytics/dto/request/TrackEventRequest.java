package com.hypermall.analytics.dto.request;

import com.hypermall.analytics.entity.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackEventRequest {

    @NotNull(message = "Event type is required")
    private EventType eventType;

    private String sessionId;
    private Long productId;
    private Long categoryId;
    private Long sellerId;
    private Long orderId;
    private String searchQuery;
    private String pageUrl;
    private String referrer;
    private String metadata;
}
