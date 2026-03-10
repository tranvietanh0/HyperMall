package com.hypermall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String keyword;

    private Long categoryId;

    private Long brandId;

    private Long sellerId;

    private Double minPrice;

    private Double maxPrice;

    private Double minRating;

    private List<String> attributes;

    private SortOption sortBy;

    private Integer page;

    private Integer size;

    public enum SortOption {
        RELEVANCE,
        PRICE_ASC,
        PRICE_DESC,
        RATING,
        NEWEST,
        BEST_SELLING
    }
}
