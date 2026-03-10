package com.hypermall.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_behaviors", indexes = {
        @Index(name = "idx_user_behavior_user", columnList = "userId"),
        @Index(name = "idx_user_behavior_product", columnList = "productId"),
        @Index(name = "idx_user_behavior_type", columnList = "behaviorType")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BehaviorType behaviorType;

    private Double score;

    @Column(length = 500)
    private String searchQuery;

    private Long categoryId;

    private Long brandId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum BehaviorType {
        VIEW,           // Xem sản phẩm (weight: 1)
        SEARCH,         // Tìm kiếm (weight: 1)
        ADD_TO_CART,    // Thêm vào giỏ (weight: 3)
        PURCHASE,       // Mua hàng (weight: 5)
        REVIEW,         // Đánh giá (weight: 4)
        WISHLIST        // Thêm wishlist (weight: 2)
    }
}
