package com.hypermall.search.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.search.dto.*;
import com.hypermall.search.service.ProductSearchService;
import com.hypermall.search.service.SearchKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Product search and autocomplete")
public class SearchController {

    private final ProductSearchService productSearchService;
    private final SearchKeywordService searchKeywordService;

    @GetMapping
    @Operation(summary = "Search products")
    public ResponseEntity<ApiResponse<SearchResultResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "RELEVANCE") ProductSearchRequest.SortOption sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .brandId(brandId)
                .sellerId(sellerId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        SearchResultResponse result = productSearchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @Operation(summary = "Search products with request body")
    public ResponseEntity<ApiResponse<SearchResultResponse>> searchWithBody(
            @RequestBody ProductSearchRequest request) {
        SearchResultResponse result = productSearchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Get autocomplete suggestions")
    public ResponseEntity<ApiResponse<AutocompleteResponse>> autocomplete(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        AutocompleteResponse result = productSearchService.autocomplete(query, limit);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending search keywords")
    public ResponseEntity<ApiResponse<List<TrendingKeywordResponse>>> getTrending(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<TrendingKeywordResponse> trending = searchKeywordService.getTrendingKeywords(limit);
        return ResponseEntity.ok(ApiResponse.success(trending));
    }

    @GetMapping("/hot")
    @Operation(summary = "Get hot search keywords (real-time)")
    public ResponseEntity<ApiResponse<List<TrendingKeywordResponse>>> getHotKeywords(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<TrendingKeywordResponse> hot = searchKeywordService.getHotKeywords(limit);
        return ResponseEntity.ok(ApiResponse.success(hot));
    }

    @PostMapping("/track-click")
    @Operation(summary = "Track keyword click for analytics")
    public ResponseEntity<ApiResponse<Void>> trackClick(@RequestParam String keyword) {
        searchKeywordService.trackClick(keyword);
        return ResponseEntity.ok(ApiResponse.success("Click tracked", null));
    }
}
