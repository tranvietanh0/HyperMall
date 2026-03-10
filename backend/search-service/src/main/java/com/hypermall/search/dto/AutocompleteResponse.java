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
public class AutocompleteResponse {

    private List<SuggestionItem> suggestions;
    private List<ProductSuggestion> products;
    private List<CategorySuggestion> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestionItem {
        private String text;
        private String highlighted;
        private long searchCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private String id;
        private String name;
        private String thumbnail;
        private Double price;
        private Double salePrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySuggestion {
        private Long id;
        private String name;
        private String path;
    }
}
