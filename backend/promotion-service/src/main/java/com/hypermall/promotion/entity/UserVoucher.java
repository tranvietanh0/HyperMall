package com.hypermall.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers", indexes = {
        @Index(name = "idx_user_voucher_user", columnList = "userId"),
        @Index(name = "idx_user_voucher_voucher", columnList = "voucher_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_voucher", columnNames = {"userId", "voucher_id", "orderId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    // Order ID if used (null if only claimed but not used yet)
    private Long orderId;

    @Builder.Default
    private boolean isUsed = false;

    private LocalDateTime usedAt;

    @CreationTimestamp
    private LocalDateTime claimedAt;
}
