package com.hypermall.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "analytics_events",
        indexes = {
                @Index(name = "idx_event_type", columnList = "event_type"),
                @Index(name = "idx_event_user_id", columnList = "user_id"),
                @Index(name = "idx_event_created_at", columnList = "created_at"),
                @Index(name = "idx_event_seller_id", columnList = "seller_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "search_query", length = 255)
    private String searchQuery;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(name = "referrer", length = 500)
    private String referrer;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "device_type", length = 30)
    private String deviceType;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
