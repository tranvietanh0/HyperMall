package com.hypermall.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vouchers", indexes = {
        @Index(name = "idx_voucher_code", columnList = "code", unique = true),
        @Index(name = "idx_voucher_status", columnList = "status"),
        @Index(name = "idx_voucher_dates", columnList = "startDate, endDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscount;

    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderValue;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageLimit = 0; // 0 = unlimited

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer userLimit = 1; // Max usage per user

    // Comma-separated category IDs (null = all categories)
    @Column(length = 1000)
    private String applicableCategories;

    // Comma-separated product IDs (null = all products)
    @Column(length = 1000)
    private String applicableProducts;

    // Seller-specific voucher (null = platform voucher)
    private Long sellerId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserVoucher> userVouchers = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == VoucherStatus.ACTIVE &&
                now.isAfter(startDate) &&
                now.isBefore(endDate) &&
                (usageLimit == 0 || usedCount < usageLimit);
    }

    public boolean isApplicableToCategory(Long categoryId) {
        if (applicableCategories == null || applicableCategories.isEmpty()) {
            return true;
        }
        return applicableCategories.contains(categoryId.toString());
    }

    public boolean isApplicableToProduct(Long productId) {
        if (applicableProducts == null || applicableProducts.isEmpty()) {
            return true;
        }
        return applicableProducts.contains(productId.toString());
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid() || orderAmount.compareTo(minOrderValue != null ? minOrderValue : BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        switch (type) {
            case PERCENTAGE:
                discount = orderAmount.multiply(value).divide(BigDecimal.valueOf(100));
                if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                    discount = maxDiscount;
                }
                break;
            case FIXED_AMOUNT:
                discount = value;
                break;
            case FREE_SHIPPING:
                discount = BigDecimal.ZERO; // Shipping discount handled separately
                break;
            default:
                discount = BigDecimal.ZERO;
        }
        return discount;
    }
}
