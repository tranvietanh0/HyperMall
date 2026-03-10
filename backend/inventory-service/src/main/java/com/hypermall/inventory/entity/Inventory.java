package com.hypermall.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories", indexes = {
        @Index(name = "idx_product_variant", columnList = "product_id, variant_id"),
        @Index(name = "idx_seller_id", columnList = "seller_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_variant", columnNames = {"product_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "is_track_quantity", nullable = false)
    @Builder.Default
    private Boolean isTrackQuantity = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Computed field
    @Transient
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    @Transient
    public boolean isLowStock() {
        return getAvailableQuantity() <= lowStockThreshold;
    }

    @Transient
    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }
}
