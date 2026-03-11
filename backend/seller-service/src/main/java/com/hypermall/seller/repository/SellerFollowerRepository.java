package com.hypermall.seller.repository;

import com.hypermall.seller.entity.SellerFollower;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerFollowerRepository extends JpaRepository<SellerFollower, Long> {

    Optional<SellerFollower> findBySellerIdAndUserId(Long sellerId, Long userId);

    boolean existsBySellerIdAndUserId(Long sellerId, Long userId);

    void deleteBySellerIdAndUserId(Long sellerId, Long userId);

    @Query("SELECT COUNT(f) FROM SellerFollower f WHERE f.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") Long sellerId);

    Page<SellerFollower> findBySellerIdOrderByFollowedAtDesc(Long sellerId, Pageable pageable);

    Page<SellerFollower> findByUserIdOrderByFollowedAtDesc(Long userId, Pageable pageable);
}
