package com.hypermall.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "seller_id"}),
        indexes = {
                @Index(name = "idx_stats_date", columnList = "stat_date"),
                @Index(name = "idx_stats_seller_id", columnList = "seller_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "total_page_views", nullable = false)
    private Long totalPageViews;

    @Column(name = "total_product_views", nullable = false)
    private Long totalProductViews;

    @Column(name = "total_add_to_cart", nullable = false)
    private Long totalAddToCart;

    @Column(name = "total_orders", nullable = false)
    private Long totalOrders;

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_users", nullable = false)
    private Long totalUsers;

    @Column(name = "new_users", nullable = false)
    private Long newUsers;

    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate;

    @Column(name = "avg_order_value", precision = 15, scale = 2)
    private BigDecimal avgOrderValue;
}
