package com.hypermall.review.repository;

import com.hypermall.review.entity.Review;
import com.hypermall.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    Page<Review> findByProductIdAndStatusAndRating(Long productId, ReviewStatus status, Integer rating, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status AND SIZE(r.images) > 0")
    Page<Review> findByProductIdWithImages(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    Page<Review> findBySellerId(Long sellerId, Pageable pageable);

    Optional<Review> findByOrderIdAndProductId(Long orderId, Long productId);

    boolean existsByOrderIdAndProductIdAndUserId(Long orderId, Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Integer countByProductIdAndStatusApproved(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' AND SIZE(r.images) > 0")
    Integer countWithImages(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' AND SIZE(r.videos) > 0")
    Integer countWithVideos(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' AND r.verifiedPurchase = true")
    Integer countVerifiedPurchases(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :id AND r.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);
}
