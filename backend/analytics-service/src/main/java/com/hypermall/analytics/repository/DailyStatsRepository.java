package com.hypermall.analytics.repository;

import com.hypermall.analytics.entity.DailyStats;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    Optional<DailyStats> findByStatDateAndSellerId(LocalDate statDate, Long sellerId);

    List<DailyStats> findByStatDateBetweenAndSellerIdOrderByStatDateAsc(
            LocalDate startDate, LocalDate endDate, Long sellerId);

    List<DailyStats> findByStatDateBetweenAndSellerIdIsNullOrderByStatDateAsc(
            LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(d.totalOrders) FROM DailyStats d WHERE d.statDate BETWEEN :start AND :end AND d.sellerId IS NULL")
    Long sumTotalOrders(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(d.totalRevenue) FROM DailyStats d WHERE d.statDate BETWEEN :start AND :end AND d.sellerId IS NULL")
    java.math.BigDecimal sumTotalRevenue(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(d.totalOrders) FROM DailyStats d WHERE d.sellerId = :sellerId AND d.statDate BETWEEN :start AND :end")
    Long sumTotalOrdersBySeller(@Param("sellerId") Long sellerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(d.totalRevenue) FROM DailyStats d WHERE d.sellerId = :sellerId AND d.statDate BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalRevenueBySeller(@Param("sellerId") Long sellerId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
