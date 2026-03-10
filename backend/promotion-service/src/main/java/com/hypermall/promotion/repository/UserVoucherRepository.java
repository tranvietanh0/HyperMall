package com.hypermall.promotion.repository;

import com.hypermall.promotion.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    List<UserVoucher> findByUserId(Long userId);

    List<UserVoucher> findByUserIdAndIsUsed(Long userId, boolean isUsed);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId AND uv.voucher.id = :voucherId AND uv.isUsed = false")
    Optional<UserVoucher> findUnusedByUserIdAndVoucherId(@Param("userId") Long userId, @Param("voucherId") Long voucherId);

    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.userId = :userId AND uv.voucher.id = :voucherId")
    long countByUserIdAndVoucherId(@Param("userId") Long userId, @Param("voucherId") Long voucherId);

    boolean existsByUserIdAndVoucherIdAndOrderId(Long userId, Long voucherId, Long orderId);
}
