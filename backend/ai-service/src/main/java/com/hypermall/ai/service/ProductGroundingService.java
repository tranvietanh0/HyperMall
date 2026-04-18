package com.hypermall.ai.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypermall.ai.config.AiProperties;
import com.hypermall.ai.dto.request.ChatRequest;
import com.hypermall.ai.dto.response.ChatResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductGroundingService {

    private final WebClient productServiceWebClient;
    private final AiProperties aiProperties;

    public GroundingResult buildGrounding(ChatRequest request) {
        ProductSnapshot currentProduct = fetchCurrentProduct(request);
        List<ChatResponse.ProductSuggestion> suggestions = new ArrayList<>();

        if (currentProduct != null) {
            suggestions.add(toSuggestion(currentProduct));
        }

        if (isProductDiscoveryIntent(request.getMessage())) {
            String keyword = resolveKeyword(request);
            suggestions.addAll(searchProducts(keyword));
        }

        List<ChatResponse.ProductSuggestion> deduplicated = deduplicateSuggestions(suggestions);
        int limit = aiProperties.getChat().getProductSuggestionLimit();
        if (deduplicated.size() > limit) {
            deduplicated = deduplicated.subList(0, limit);
        }

        return new GroundingResult(currentProduct, deduplicated);
    }

    private ProductSnapshot fetchCurrentProduct(ChatRequest request) {
        ChatRequest.ChatContext context = request.getContext();
        if (context == null || context.getProductId() == null) {
            return null;
        }

        try {
            ProductApiResponse<ProductDetailPayload> response = productServiceWebClient.get()
                    .uri("/api/products/{id}", context.getProductId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ProductApiResponse<ProductDetailPayload>>() {})
                    .block();

            if (response == null || response.getData() == null) {
                return null;
            }

            ProductDetailPayload data = response.getData();
            return ProductSnapshot.builder()
                    .id(data.getId())
                    .name(data.getName())
                    .thumbnail(data.getThumbnail())
                    .shortDescription(data.getShortDescription())
                    .categoryName(data.getCategory() != null ? data.getCategory().getName() : null)
                    .price(data.resolveDisplayPrice())
                    .avgRating(data.getAvgRating())
                    .build();
        } catch (Exception ex) {
            log.warn("Could not fetch current product context: {}", ex.getMessage());
            return null;
        }
    }

    private List<ChatResponse.ProductSuggestion> searchProducts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        try {
            ProductApiResponse<PagePayload<ProductSummaryPayload>> response = productServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products")
                            .queryParam("keyword", keyword)
                            .queryParam("page", 0)
                            .queryParam("size", aiProperties.getChat().getProductSuggestionLimit())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ProductApiResponse<PagePayload<ProductSummaryPayload>>>() {})
                    .block();

            if (response == null || response.getData() == null || response.getData().getContent() == null) {
                return List.of();
            }

            return response.getData().getContent().stream()
                    .map(item -> ChatResponse.ProductSuggestion.builder()
                            .productId(item.getId())
                            .productName(item.getName())
                            .thumbnail(item.getThumbnail())
                            .price(resolvePrice(item.getSalePrice(), item.getBasePrice()))
                            .build())
                    .toList();
        } catch (Exception ex) {
            log.warn("Could not search products for grounding: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<ChatResponse.ProductSuggestion> deduplicateSuggestions(List<ChatResponse.ProductSuggestion> suggestions) {
        Map<Long, ChatResponse.ProductSuggestion> unique = new LinkedHashMap<>();
        for (ChatResponse.ProductSuggestion suggestion : suggestions) {
            if (suggestion.getProductId() != null) {
                unique.putIfAbsent(suggestion.getProductId(), suggestion);
            }
        }
        return new ArrayList<>(unique.values());
    }

    String resolveKeyword(ChatRequest request) {
        ChatRequest.ChatContext context = request.getContext();
        if (context != null
                && StringUtils.hasText(context.getProductName())
                && isComparativeProductIntent(request.getMessage())) {
            return request.getMessage() + " " + context.getProductName();
        }
        return request.getMessage();
    }

    boolean isProductDiscoveryIntent(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("find")
                || normalized.contains("recommend")
                || normalized.contains("suggest")
                || normalized.contains("budget")
                || normalized.contains("cheap")
                || normalized.contains("under ")
                || normalized.contains("tim ")
                || normalized.contains("tìm ")
                || normalized.contains("goi y")
                || normalized.contains("gợi ý")
                || normalized.contains("de xuat")
                || normalized.contains("đề xuất")
                || normalized.contains("mua ")
                || normalized.contains("nên mua")
                || normalized.contains("duoi")
                || normalized.contains("cái nào")
                || normalized.contains("loại nào")
                || normalized.contains("tot hon")
                || normalized.contains("tốt hơn")
                || normalized.contains("re hon")
                || normalized.contains("rẻ hơn")
                || normalized.contains("phu hop")
                || normalized.contains("phù hợp")
                || normalized.contains("dưới");
    }

    private boolean isComparativeProductIntent(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("this product")
                || normalized.contains("sản phẩm này")
                || normalized.contains("cái này")
                || normalized.contains("mẫu này")
                || normalized.contains("tot hon")
                || normalized.contains("tốt hơn")
                || normalized.contains("re hon")
                || normalized.contains("rẻ hơn")
                || normalized.contains("phu hop")
                || normalized.contains("phù hợp");
    }

    private ChatResponse.ProductSuggestion toSuggestion(ProductSnapshot snapshot) {
        return ChatResponse.ProductSuggestion.builder()
                .productId(snapshot.getId())
                .productName(snapshot.getName())
                .thumbnail(snapshot.getThumbnail())
                .price(snapshot.getPrice())
                .build();
    }

    private Double resolvePrice(BigDecimal salePrice, BigDecimal basePrice) {
        BigDecimal candidate = salePrice != null ? salePrice : basePrice;
        return candidate != null ? candidate.doubleValue() : null;
    }

    @Data
    @lombok.Builder
    public static class ProductSnapshot {
        private Long id;
        private String name;
        private String categoryName;
        private String shortDescription;
        private String thumbnail;
        private Double price;
        private Double avgRating;
    }

    public record GroundingResult(ProductSnapshot currentProduct,
                                  List<ChatResponse.ProductSuggestion> productSuggestions) {
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProductApiResponse<T> {
        private T data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagePayload<T> {
        private List<T> content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProductSummaryPayload {
        private Long id;
        private String name;
        private String thumbnail;
        private BigDecimal basePrice;
        private BigDecimal salePrice;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProductDetailPayload {
        private Long id;
        private String name;
        private String thumbnail;
        private String shortDescription;
        private CategoryPayload category;
        private BigDecimal basePrice;
        private BigDecimal salePrice;
        private Double avgRating;

        private Double resolveDisplayPrice() {
            BigDecimal candidate = salePrice != null ? salePrice : basePrice;
            return candidate != null ? candidate.doubleValue() : null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CategoryPayload {
        private String name;
    }
}
