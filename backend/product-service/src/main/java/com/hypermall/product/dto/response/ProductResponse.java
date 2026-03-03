package com.hypermall.product.dto.response;

import com.hypermall.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String name;
    private String slug;
    private String shortDescription;
    private String thumbnail;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private ProductStatus status;
    private Integer totalSold;
    private Double avgRating;
    private Integer totalReviews;
    private Boolean hasVariants;
    private LocalDateTime createdAt;

    public BigDecimal getDiscountPercentage() {
        if (salePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = basePrice.subtract(salePrice);
            return discount.multiply(BigDecimal.valueOf(100)).divide(basePrice, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
