package com.hypermall.analytics.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long totalProducts;
    private Long totalUsers;
    private Long totalPageViews;
    private BigDecimal conversionRate;
    private BigDecimal avgOrderValue;

    private BigDecimal ordersChangePercent;
    private BigDecimal revenueChangePercent;
    private BigDecimal usersChangePercent;

    private List<DailyStatsItem> dailyStats;
    private List<TopProductItem> topProducts;
    private List<TopCategoryItem> topCategories;
    private Map<String, Long> ordersByStatus;
}
