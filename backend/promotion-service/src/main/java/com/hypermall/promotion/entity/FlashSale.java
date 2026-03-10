package com.hypermall.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flash_sales", indexes = {
        @Index(name = "idx_flash_sale_status", columnList = "status"),
        @Index(name = "idx_flash_sale_dates", columnList = "startTime, endTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String bannerImage;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FlashSaleStatus status = FlashSaleStatus.SCHEDULED;

    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FlashSaleProduct> products = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == FlashSaleStatus.ACTIVE &&
                now.isAfter(startTime) &&
                now.isBefore(endTime);
    }

    public boolean isUpcoming() {
        return status == FlashSaleStatus.SCHEDULED &&
                LocalDateTime.now().isBefore(startTime);
    }
}
