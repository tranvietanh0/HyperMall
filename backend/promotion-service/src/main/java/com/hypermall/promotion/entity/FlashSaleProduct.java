package com.hypermall.promotion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_products", indexes = {
        @Index(name = "idx_fsp_product", columnList = "productId"),
        @Index(name = "idx_fsp_flash_sale", columnList = "flash_sale_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @Column(nullable = false)
    private Long productId;

    private Long variantId;

    // Product info snapshot (for display without calling product service)
    @Column(nullable = false)
    private String productName;

    private String productImage;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal flashSalePrice;

    @Column(nullable = false)
    private Integer stockLimit;

    @Column(nullable = false)
    @Builder.Default
    private Integer soldCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    public boolean isAvailable() {
        return soldCount < stockLimit;
    }

    public int getDiscountPercent() {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) return 0;
        BigDecimal discount = originalPrice.subtract(flashSalePrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP);
        return discount.intValue();
    }

    public int getRemainingStock() {
        return stockLimit - soldCount;
    }
}
