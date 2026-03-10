package com.hypermall.review.repository;

import com.hypermall.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    @Query("SELECT rl.reviewId FROM ReviewLike rl WHERE rl.userId = :userId AND rl.reviewId IN :reviewIds")
    List<Long> findLikedReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}
