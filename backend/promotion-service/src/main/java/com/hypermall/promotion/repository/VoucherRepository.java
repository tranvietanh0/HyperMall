package com.hypermall.promotion.repository;

import com.hypermall.promotion.entity.Voucher;
import com.hypermall.promotion.entity.VoucherStatus;
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
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    Page<Voucher> findByStatus(VoucherStatus status, Pageable pageable);

    Page<Voucher> findBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE v.status = :status " +
            "AND v.startDate <= :now AND v.endDate >= :now " +
            "AND (v.usageLimit = 0 OR v.usedCount < v.usageLimit)")
    List<Voucher> findActiveVouchers(@Param("status") VoucherStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' " +
            "AND v.startDate <= :now AND v.endDate >= :now " +
            "AND (v.sellerId IS NULL OR v.sellerId = :sellerId) " +
            "AND (v.usageLimit = 0 OR v.usedCount < v.usageLimit)")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now, @Param("sellerId") Long sellerId);

    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' " +
            "AND v.endDate < :now")
    List<Voucher> findExpiredVouchers(@Param("now") LocalDateTime now);
}
