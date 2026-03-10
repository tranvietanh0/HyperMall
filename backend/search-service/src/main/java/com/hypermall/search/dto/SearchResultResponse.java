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
public class SearchResultResponse {

    private List<ProductSearchResponse> products;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private Map<String, List<FacetValue>> facets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacetValue {
        private String value;
        private long count;
    }
}
