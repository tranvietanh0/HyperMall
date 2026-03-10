package com.hypermall.ai.repository;

import com.hypermall.ai.entity.UserBehavior;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    List<UserBehavior> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<UserBehavior> findByUserIdAndBehaviorType(Long userId, UserBehavior.BehaviorType behaviorType);

    @Query("SELECT b.productId, SUM(b.score) as totalScore FROM UserBehavior b " +
            "WHERE b.userId = :userId AND b.createdAt > :since " +
            "GROUP BY b.productId ORDER BY totalScore DESC")
    List<Object[]> findTopProductsByUserScore(@Param("userId") Long userId,
                                               @Param("since") LocalDateTime since,
                                               Pageable pageable);

    @Query("SELECT b.categoryId, COUNT(b) as count FROM UserBehavior b " +
            "WHERE b.userId = :userId AND b.categoryId IS NOT NULL " +
            "GROUP BY b.categoryId ORDER BY count DESC")
    List<Object[]> findTopCategoriesByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b.brandId, COUNT(b) as count FROM UserBehavior b " +
            "WHERE b.userId = :userId AND b.brandId IS NOT NULL " +
            "GROUP BY b.brandId ORDER BY count DESC")
    List<Object[]> findTopBrandsByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ub2.productId FROM UserBehavior ub1 " +
            "JOIN UserBehavior ub2 ON ub1.userId = ub2.userId " +
            "WHERE ub1.productId = :productId AND ub2.productId != :productId " +
            "AND ub1.behaviorType = 'PURCHASE' AND ub2.behaviorType = 'PURCHASE' " +
            "GROUP BY ub2.productId ORDER BY COUNT(ub2.productId) DESC")
    List<Long> findFrequentlyBoughtTogether(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT b.productId, COUNT(b) as viewCount FROM UserBehavior b " +
            "WHERE b.behaviorType = 'VIEW' AND b.createdAt > :since " +
            "GROUP BY b.productId ORDER BY viewCount DESC")
    List<Object[]> findTrendingProducts(@Param("since") LocalDateTime since, Pageable pageable);
}
