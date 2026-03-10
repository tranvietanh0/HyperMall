package com.hypermall.promotion.repository;

import com.hypermall.promotion.entity.FlashSale;
import com.hypermall.promotion.entity.FlashSaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    Page<FlashSale> findByStatus(FlashSaleStatus status, Pageable pageable);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
            "AND fs.startTime <= :now AND fs.endTime >= :now")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
            "AND fs.startTime <= :now AND fs.endTime >= :now")
    Optional<FlashSale> findCurrentFlashSale(@Param("now") LocalDateTime now);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'SCHEDULED' " +
            "AND fs.startTime > :now ORDER BY fs.startTime ASC")
    List<FlashSale> findUpcomingFlashSales(@Param("now") LocalDateTime now);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'SCHEDULED' " +
            "AND fs.startTime <= :now")
    List<FlashSale> findFlashSalesToActivate(@Param("now") LocalDateTime now);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
            "AND fs.endTime <= :now")
    List<FlashSale> findFlashSalesToEnd(@Param("now") LocalDateTime now);
}
