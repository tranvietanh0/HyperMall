package com.hypermall.analytics.service;

import com.hypermall.analytics.dto.request.TrackEventRequest;
import com.hypermall.analytics.dto.response.DailyStatsItem;
import com.hypermall.analytics.dto.response.DashboardStatsResponse;
import com.hypermall.analytics.dto.response.TopCategoryItem;
import com.hypermall.analytics.dto.response.TopProductItem;
import com.hypermall.analytics.entity.AnalyticsEvent;
import com.hypermall.analytics.entity.DailyStats;
import com.hypermall.analytics.entity.EventType;
import com.hypermall.analytics.repository.AnalyticsEventRepository;
import com.hypermall.analytics.repository.DailyStatsRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsEventRepository eventRepository;
    private final DailyStatsRepository dailyStatsRepository;

    @Async
    @Transactional
    public void trackEvent(Long userId, TrackEventRequest request, HttpServletRequest httpRequest) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventType(request.getEventType())
                .userId(userId)
                .sessionId(request.getSessionId())
                .productId(request.getProductId())
                .categoryId(request.getCategoryId())
                .sellerId(request.getSellerId())
                .orderId(request.getOrderId())
                .searchQuery(request.getSearchQuery())
                .pageUrl(request.getPageUrl())
                .referrer(request.getReferrer())
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .ipAddress(httpRequest != null ? getClientIp(httpRequest) : null)
                .deviceType(httpRequest != null ? detectDeviceType(httpRequest.getHeader("User-Agent")) : null)
                .metadata(request.getMetadata())
                .build();

        eventRepository.save(event);
        log.debug("Tracked event: type={}, userId={}", request.getEventType(), userId);
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(Long sellerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        LocalDate prevStartDate = startDate.minusDays(startDate.until(endDate).getDays() + 1);

        Long totalOrders;
        BigDecimal totalRevenue;
        Long prevOrders;
        BigDecimal prevRevenue;

        if (sellerId == null) {
            totalOrders = dailyStatsRepository.sumTotalOrders(startDate, endDate);
            totalRevenue = dailyStatsRepository.sumTotalRevenue(startDate, endDate);
            prevOrders = dailyStatsRepository.sumTotalOrders(prevStartDate, startDate.minusDays(1));
            prevRevenue = dailyStatsRepository.sumTotalRevenue(prevStartDate, startDate.minusDays(1));
        } else {
            totalOrders = dailyStatsRepository.sumTotalOrdersBySeller(sellerId, startDate, endDate);
            totalRevenue = dailyStatsRepository.sumTotalRevenueBySeller(sellerId, startDate, endDate);
            prevOrders = dailyStatsRepository.sumTotalOrdersBySeller(sellerId, prevStartDate, startDate.minusDays(1));
            prevRevenue = dailyStatsRepository.sumTotalRevenueBySeller(sellerId, prevStartDate, startDate.minusDays(1));
        }

        totalOrders = totalOrders != null ? totalOrders : 0L;
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        prevOrders = prevOrders != null ? prevOrders : 0L;
        prevRevenue = prevRevenue != null ? prevRevenue : BigDecimal.ZERO;

        long totalPageViews = sellerId == null
                ? eventRepository.countByEventTypeAndCreatedAtBetween(EventType.PAGE_VIEW, start, end)
                : eventRepository.countBySellerIdAndEventTypeAndCreatedAtBetween(sellerId, EventType.PAGE_VIEW, start, end);

        long totalUsers = sellerId == null
                ? eventRepository.countDistinctUsersBetween(start, end)
                : eventRepository.countDistinctUsersBySellerBetween(sellerId, start, end);

        BigDecimal conversionRate = totalPageViews > 0
                ? BigDecimal.valueOf(totalOrders * 100.0 / totalPageViews).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<DailyStats> dailyStatsList = sellerId == null
                ? dailyStatsRepository.findByStatDateBetweenAndSellerIdIsNullOrderByStatDateAsc(startDate, endDate)
                : dailyStatsRepository.findByStatDateBetweenAndSellerIdOrderByStatDateAsc(startDate, endDate, sellerId);

        List<DailyStatsItem> dailyItems = dailyStatsList.stream()
                .map(ds -> DailyStatsItem.builder()
                        .date(ds.getStatDate())
                        .orders(ds.getTotalOrders())
                        .revenue(ds.getTotalRevenue())
                        .pageViews(ds.getTotalPageViews())
                        .users(ds.getTotalUsers())
                        .build())
                .toList();

        List<Object[]> topProductsRaw = eventRepository.findTopProductsByEventType(EventType.PRODUCT_VIEW, start, end);
        List<TopProductItem> topProducts = new ArrayList<>();
        for (int i = 0; i < Math.min(10, topProductsRaw.size()); i++) {
            Object[] row = topProductsRaw.get(i);
            topProducts.add(TopProductItem.builder()
                    .productId((Long) row[0])
                    .productName("Product " + row[0])
                    .views((Long) row[1])
                    .sales(0L)
                    .revenue(BigDecimal.ZERO)
                    .build());
        }

        List<Object[]> topCategoriesRaw = eventRepository.findTopCategoriesByEventType(EventType.PRODUCT_VIEW, start, end);
        List<TopCategoryItem> topCategories = new ArrayList<>();
        for (int i = 0; i < Math.min(10, topCategoriesRaw.size()); i++) {
            Object[] row = topCategoriesRaw.get(i);
            topCategories.add(TopCategoryItem.builder()
                    .categoryId((Long) row[0])
                    .categoryName("Category " + row[0])
                    .views((Long) row[1])
                    .sales(0L)
                    .revenue(BigDecimal.ZERO)
                    .build());
        }

        BigDecimal ordersChange = calculateChangePercent(totalOrders, prevOrders);
        BigDecimal revenueChange = calculateChangePercent(totalRevenue, prevRevenue);

        return DashboardStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalProducts(0L)
                .totalUsers(totalUsers)
                .totalPageViews(totalPageViews)
                .conversionRate(conversionRate)
                .avgOrderValue(avgOrderValue)
                .ordersChangePercent(ordersChange)
                .revenueChangePercent(revenueChange)
                .usersChangePercent(BigDecimal.ZERO)
                .dailyStats(dailyItems)
                .topProducts(topProducts)
                .topCategories(topCategories)
                .ordersByStatus(new HashMap<>())
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getTopSearchQueries(int limit, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = eventRepository.findTopSearchQueries(start, end);
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, results.size()); i++) {
            queries.add((String) results.get(i)[0]);
        }
        return queries;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return "unknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        }
        return "desktop";
    }

    private BigDecimal calculateChangePercent(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf((current - previous) * 100.0 / previous).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateChangePercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }
}
