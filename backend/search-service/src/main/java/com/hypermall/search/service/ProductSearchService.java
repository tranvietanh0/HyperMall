package com.hypermall.search.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.hypermall.search.document.ProductDocument;
import com.hypermall.search.dto.*;
import com.hypermall.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchKeywordService searchKeywordService;

    public SearchResultResponse search(ProductSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;

        Criteria criteria = buildSearchCriteria(request);
        Sort sort = buildSort(request.getSortBy());

        CriteriaQuery query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(page, size, sort));

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        // Track search keyword
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            searchKeywordService.trackSearch(request.getKeyword());
        }

        List<ProductSearchResponse> products = searchHits.getSearchHits().stream()
                .map(hit -> mapToResponse(hit.getContent()))
                .collect(Collectors.toList());

        Map<String, List<SearchResultResponse.FacetValue>> facets = buildFacets(searchHits);

        return SearchResultResponse.builder()
                .products(products)
                .totalElements(searchHits.getTotalHits())
                .totalPages((int) Math.ceil((double) searchHits.getTotalHits() / size))
                .currentPage(page)
                .pageSize(size)
                .facets(facets)
                .build();
    }

    private Criteria buildSearchCriteria(ProductSearchRequest request) {
        Criteria criteria = new Criteria();

        // Only show active products
        criteria = criteria.and("status").is("ACTIVE");

        // Keyword search
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            Criteria keywordCriteria = new Criteria("name").contains(request.getKeyword())
                    .or("description").contains(request.getKeyword())
                    .or("brandName").contains(request.getKeyword())
                    .or("categoryName").contains(request.getKeyword());
            criteria = criteria.and(keywordCriteria);
        }

        // Category filter
        if (request.getCategoryId() != null) {
            criteria = criteria.and("categoryId").is(request.getCategoryId());
        }

        // Brand filter
        if (request.getBrandId() != null) {
            criteria = criteria.and("brandId").is(request.getBrandId());
        }

        // Seller filter
        if (request.getSellerId() != null) {
            criteria = criteria.and("sellerId").is(request.getSellerId());
        }

        // Price range
        if (request.getMinPrice() != null) {
            criteria = criteria.and("salePrice").greaterThanEqual(request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            criteria = criteria.and("salePrice").lessThanEqual(request.getMaxPrice());
        }

        // Rating filter
        if (request.getMinRating() != null) {
            criteria = criteria.and("rating").greaterThanEqual(request.getMinRating());
        }

        return criteria;
    }

    private Sort buildSort(ProductSearchRequest.SortOption sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "totalSold");
        }

        return switch (sortBy) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "salePrice");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "salePrice");
            case RATING -> Sort.by(Sort.Direction.DESC, "rating");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case BEST_SELLING -> Sort.by(Sort.Direction.DESC, "totalSold");
            default -> Sort.by(Sort.Direction.DESC, "_score");
        };
    }

    private Map<String, List<SearchResultResponse.FacetValue>> buildFacets(SearchHits<ProductDocument> searchHits) {
        Map<String, List<SearchResultResponse.FacetValue>> facets = new HashMap<>();

        // Category facets
        Map<String, Long> categoryCount = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getCategoryName())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        facets.put("categories", categoryCount.entrySet().stream()
                .map(e -> SearchResultResponse.FacetValue.builder()
                        .value(e.getKey())
                        .count(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(10)
                .collect(Collectors.toList()));

        // Brand facets
        Map<String, Long> brandCount = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getBrandName())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        facets.put("brands", brandCount.entrySet().stream()
                .map(e -> SearchResultResponse.FacetValue.builder()
                        .value(e.getKey())
                        .count(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(10)
                .collect(Collectors.toList()));

        // Price range facets
        facets.put("priceRanges", buildPriceRangeFacets(searchHits));

        return facets;
    }

    private List<SearchResultResponse.FacetValue> buildPriceRangeFacets(SearchHits<ProductDocument> searchHits) {
        List<SearchResultResponse.FacetValue> priceRanges = new ArrayList<>();

        long under100k = searchHits.getSearchHits().stream()
                .filter(h -> h.getContent().getSalePrice() != null && h.getContent().getSalePrice() < 100000)
                .count();

        long from100kTo500k = searchHits.getSearchHits().stream()
                .filter(h -> h.getContent().getSalePrice() != null
                        && h.getContent().getSalePrice() >= 100000
                        && h.getContent().getSalePrice() < 500000)
                .count();

        long from500kTo1m = searchHits.getSearchHits().stream()
                .filter(h -> h.getContent().getSalePrice() != null
                        && h.getContent().getSalePrice() >= 500000
                        && h.getContent().getSalePrice() < 1000000)
                .count();

        long over1m = searchHits.getSearchHits().stream()
                .filter(h -> h.getContent().getSalePrice() != null && h.getContent().getSalePrice() >= 1000000)
                .count();

        if (under100k > 0) {
            priceRanges.add(SearchResultResponse.FacetValue.builder().value("0-100000").count(under100k).build());
        }
        if (from100kTo500k > 0) {
            priceRanges.add(SearchResultResponse.FacetValue.builder().value("100000-500000").count(from100kTo500k).build());
        }
        if (from500kTo1m > 0) {
            priceRanges.add(SearchResultResponse.FacetValue.builder().value("500000-1000000").count(from500kTo1m).build());
        }
        if (over1m > 0) {
            priceRanges.add(SearchResultResponse.FacetValue.builder().value("1000000+").count(over1m).build());
        }

        return priceRanges;
    }

    public AutocompleteResponse autocomplete(String query, int limit) {
        if (query == null || query.isBlank()) {
            return AutocompleteResponse.builder()
                    .suggestions(Collections.emptyList())
                    .products(Collections.emptyList())
                    .categories(Collections.emptyList())
                    .build();
        }

        // Get keyword suggestions
        List<AutocompleteResponse.SuggestionItem> suggestions = searchKeywordService
                .getSuggestions(query, limit);

        // Get product suggestions
        List<ProductDocument> productDocs = productSearchRepository
                .findByNameContainingIgnoreCaseOrderByTotalSoldDesc(query, PageRequest.of(0, limit));

        List<AutocompleteResponse.ProductSuggestion> products = productDocs.stream()
                .map(doc -> AutocompleteResponse.ProductSuggestion.builder()
                        .id(doc.getId())
                        .name(doc.getName())
                        .thumbnail(doc.getThumbnail())
                        .price(doc.getPrice())
                        .salePrice(doc.getSalePrice())
                        .build())
                .collect(Collectors.toList());

        // Get category suggestions from products
        Set<String> seenCategories = new HashSet<>();
        List<AutocompleteResponse.CategorySuggestion> categories = productDocs.stream()
                .filter(doc -> doc.getCategoryName() != null && seenCategories.add(doc.getCategoryName()))
                .map(doc -> AutocompleteResponse.CategorySuggestion.builder()
                        .id(doc.getCategoryId())
                        .name(doc.getCategoryName())
                        .path(doc.getCategoryPath() != null ? String.join(" > ", doc.getCategoryPath()) : null)
                        .build())
                .limit(5)
                .collect(Collectors.toList());

        return AutocompleteResponse.builder()
                .suggestions(suggestions)
                .products(products)
                .categories(categories)
                .build();
    }

    public void indexProduct(IndexProductRequest request) {
        ProductDocument document = ProductDocument.builder()
                .id(String.valueOf(request.getProductId()))
                .name(request.getName())
                .nameKeyword(request.getName())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .categoryName(request.getCategoryName())
                .categoryPath(request.getCategoryPath())
                .brandId(request.getBrandId())
                .brandName(request.getBrandName())
                .sellerId(request.getSellerId())
                .sellerName(request.getSellerName())
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .thumbnail(request.getThumbnail())
                .rating(request.getRating())
                .totalReviews(request.getTotalReviews())
                .totalSold(request.getTotalSold())
                .status(request.getStatus())
                .attributes(request.getAttributes())
                .tags(request.getTags())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productSearchRepository.save(document);
        log.info("Indexed product: {}", request.getProductId());
    }

    public void indexProducts(List<IndexProductRequest> requests) {
        List<ProductDocument> documents = requests.stream()
                .map(req -> ProductDocument.builder()
                        .id(String.valueOf(req.getProductId()))
                        .name(req.getName())
                        .nameKeyword(req.getName())
                        .description(req.getDescription())
                        .categoryId(req.getCategoryId())
                        .categoryName(req.getCategoryName())
                        .categoryPath(req.getCategoryPath())
                        .brandId(req.getBrandId())
                        .brandName(req.getBrandName())
                        .sellerId(req.getSellerId())
                        .sellerName(req.getSellerName())
                        .price(req.getPrice())
                        .salePrice(req.getSalePrice())
                        .thumbnail(req.getThumbnail())
                        .rating(req.getRating())
                        .totalReviews(req.getTotalReviews())
                        .totalSold(req.getTotalSold())
                        .status(req.getStatus())
                        .attributes(req.getAttributes())
                        .tags(req.getTags())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        productSearchRepository.saveAll(documents);
        log.info("Indexed {} products", documents.size());
    }

    public void deleteProduct(Long productId) {
        productSearchRepository.deleteById(String.valueOf(productId));
        log.info("Deleted product from index: {}", productId);
    }

    public void deleteProducts(List<Long> productIds) {
        List<String> ids = productIds.stream().map(String::valueOf).collect(Collectors.toList());
        productSearchRepository.deleteByIdIn(ids);
        log.info("Deleted {} products from index", productIds.size());
    }

    private ProductSearchResponse mapToResponse(ProductDocument doc) {
        return ProductSearchResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .description(doc.getDescription())
                .categoryId(doc.getCategoryId())
                .categoryName(doc.getCategoryName())
                .brandId(doc.getBrandId())
                .brandName(doc.getBrandName())
                .sellerId(doc.getSellerId())
                .sellerName(doc.getSellerName())
                .price(doc.getPrice())
                .salePrice(doc.getSalePrice())
                .thumbnail(doc.getThumbnail())
                .rating(doc.getRating())
                .totalReviews(doc.getTotalReviews())
                .totalSold(doc.getTotalSold())
                .attributes(doc.getAttributes())
                .build();
    }
}
