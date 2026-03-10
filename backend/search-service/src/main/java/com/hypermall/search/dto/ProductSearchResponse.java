package com.hypermall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

    private String id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long sellerId;
    private String sellerName;
    private Double price;
    private Double salePrice;
    private String thumbnail;
    private Double rating;
    private Integer totalReviews;
    private Integer totalSold;
    private Map<String, String> attributes;
    private Double discountPercentage;

    public Double getDiscountPercentage() {
        if (price != null && salePrice != null && price > 0 && salePrice < price) {
            return Math.round((price - salePrice) / price * 100 * 100.0) / 100.0;
        }
        return 0.0;
    }
}
