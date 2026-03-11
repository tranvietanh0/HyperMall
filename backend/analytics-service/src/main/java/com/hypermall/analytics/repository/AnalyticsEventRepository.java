package com.hypermall.analytics.repository;

import com.hypermall.analytics.entity.AnalyticsEvent;
import com.hypermall.analytics.entity.EventType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByEventTypeAndCreatedAtBetween(EventType eventType, LocalDateTime start, LocalDateTime end);

    long countBySellerIdAndEventTypeAndCreatedAtBetween(
            Long sellerId, EventType eventType, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM AnalyticsEvent e WHERE e.createdAt BETWEEN :start AND :end AND e.userId IS NOT NULL")
    long countDistinctUsersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM AnalyticsEvent e WHERE e.sellerId = :sellerId AND e.createdAt BETWEEN :start AND :end AND e.userId IS NOT NULL")
    long countDistinctUsersBySellerBetween(@Param("sellerId") Long sellerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e.productId, COUNT(e) as cnt FROM AnalyticsEvent e WHERE e.eventType = :eventType AND e.createdAt BETWEEN :start AND :end AND e.productId IS NOT NULL GROUP BY e.productId ORDER BY cnt DESC")
    List<Object[]> findTopProductsByEventType(@Param("eventType") EventType eventType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e.categoryId, COUNT(e) as cnt FROM AnalyticsEvent e WHERE e.eventType = :eventType AND e.createdAt BETWEEN :start AND :end AND e.categoryId IS NOT NULL GROUP BY e.categoryId ORDER BY cnt DESC")
    List<Object[]> findTopCategoriesByEventType(@Param("eventType") EventType eventType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e.searchQuery, COUNT(e) as cnt FROM AnalyticsEvent e WHERE e.eventType = 'SEARCH' AND e.createdAt BETWEEN :start AND :end AND e.searchQuery IS NOT NULL GROUP BY e.searchQuery ORDER BY cnt DESC")
    List<Object[]> findTopSearchQueries(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
