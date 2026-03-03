package com.hypermall.product.dto.response;

import com.hypermall.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private Long sellerId;
    private CategoryResponse category;
    private BrandResponse brand;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String thumbnail;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private ProductStatus status;
    private Integer totalSold;
    private Double avgRating;
    private Integer totalReviews;
    private Boolean hasVariants;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getDiscountPercentage() {
        if (salePrice != null && basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = basePrice.subtract(salePrice);
            return discount.multiply(BigDecimal.valueOf(100)).divide(basePrice, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
