package com.hypermall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexProductRequest {

    private Long productId;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private List<String> categoryPath;
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
    private String status;
    private Map<String, String> attributes;
    private List<String> tags;
}
