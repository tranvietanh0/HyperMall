package com.hypermall.ai.service;

import com.hypermall.ai.dto.RecommendationRequest;
import com.hypermall.ai.dto.RecommendationResponse;
import com.hypermall.ai.entity.UserBehavior;
import com.hypermall.ai.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final UserBehaviorRepository behaviorRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RECOMMENDATION_CACHE_PREFIX = "recommend:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    // Behavior weights
    private static final Map<UserBehavior.BehaviorType, Double> BEHAVIOR_WEIGHTS = Map.of(
            UserBehavior.BehaviorType.VIEW, 1.0,
            UserBehavior.BehaviorType.SEARCH, 1.0,
            UserBehavior.BehaviorType.WISHLIST, 2.0,
            UserBehavior.BehaviorType.ADD_TO_CART, 3.0,
            UserBehavior.BehaviorType.REVIEW, 4.0,
            UserBehavior.BehaviorType.PURCHASE, 5.0
    );

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        int limit = request.getLimit() != null ? request.getLimit() : 10;

        return switch (request.getType()) {
            case PERSONALIZED -> getPersonalizedRecommendations(request.getUserId(), limit);
            case SIMILAR_PRODUCTS -> getSimilarProducts(request.getProductId(), limit);
            case FREQUENTLY_BOUGHT -> getFrequentlyBoughtTogether(request.getProductId(), limit);
            case TRENDING -> getTrendingProducts(limit);
            case NEW_ARRIVALS -> getNewArrivals(limit);
        };
    }

    private RecommendationResponse getPersonalizedRecommendations(Long userId, int limit) {
        if (userId == null) {
            return getTrendingProducts(limit);
        }

        String cacheKey = RECOMMENDATION_CACHE_PREFIX + "personalized:" + userId;

        // Try cache first
        @SuppressWarnings("unchecked")
        List<RecommendationResponse.RecommendedProduct> cached =
                (List<RecommendationResponse.RecommendedProduct>) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return RecommendationResponse.builder()
                    .type("PERSONALIZED")
                    .products(cached)
                    .reason("Based on your browsing history")
                    .build();
        }

        // Get user's top categories
        List<Object[]> topCategories = behaviorRepository.findTopCategoriesByUser(userId, PageRequest.of(0, 3));

        // Get user's top brands
        List<Object[]> topBrands = behaviorRepository.findTopBrandsByUser(userId, PageRequest.of(0, 3));

        // Get recently interacted products to exclude
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> recentProducts = behaviorRepository.findTopProductsByUserScore(userId, since, PageRequest.of(0, 50));

        Set<Long> recentProductIds = recentProducts.stream()
                .map(arr -> (Long) arr[0])
                .collect(Collectors.toSet());

        // Build recommendations based on preferences
        List<RecommendationResponse.RecommendedProduct> recommendations = buildPersonalizedList(
                topCategories, topBrands, recentProductIds, limit);

        // Cache the result
        if (!recommendations.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, recommendations, CACHE_TTL);
        }

        return RecommendationResponse.builder()
                .type("PERSONALIZED")
                .products(recommendations)
                .reason("Based on your browsing history")
                .build();
    }

    private RecommendationResponse getSimilarProducts(Long productId, int limit) {
        if (productId == null) {
            return RecommendationResponse.builder()
                    .type("SIMILAR_PRODUCTS")
                    .products(Collections.emptyList())
                    .reason("No product specified")
                    .build();
        }

        // In a real implementation, this would use product embeddings or content-based filtering
        // For now, we'll use collaborative filtering (users who viewed this also viewed)

        String cacheKey = RECOMMENDATION_CACHE_PREFIX + "similar:" + productId;

        @SuppressWarnings("unchecked")
        List<RecommendationResponse.RecommendedProduct> cached =
                (List<RecommendationResponse.RecommendedProduct>) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return RecommendationResponse.builder()
                    .type("SIMILAR_PRODUCTS")
                    .products(cached)
                    .reason("Similar to this product")
                    .build();
        }

        // Placeholder for actual implementation
        List<RecommendationResponse.RecommendedProduct> recommendations = new ArrayList<>();

        return RecommendationResponse.builder()
                .type("SIMILAR_PRODUCTS")
                .products(recommendations)
                .reason("Similar to this product")
                .build();
    }

    private RecommendationResponse getFrequentlyBoughtTogether(Long productId, int limit) {
        if (productId == null) {
            return RecommendationResponse.builder()
                    .type("FREQUENTLY_BOUGHT")
                    .products(Collections.emptyList())
                    .reason("No product specified")
                    .build();
        }

        List<Long> productIds = behaviorRepository.findFrequentlyBoughtTogether(productId, PageRequest.of(0, limit));

        // In real implementation, fetch product details from product-service
        List<RecommendationResponse.RecommendedProduct> recommendations = productIds.stream()
                .map(id -> RecommendationResponse.RecommendedProduct.builder()
                        .productId(id)
                        .reason("Frequently bought together")
                        .build())
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .type("FREQUENTLY_BOUGHT")
                .products(recommendations)
                .reason("Customers also bought")
                .build();
    }

    private RecommendationResponse getTrendingProducts(int limit) {
        String cacheKey = RECOMMENDATION_CACHE_PREFIX + "trending";

        @SuppressWarnings("unchecked")
        List<RecommendationResponse.RecommendedProduct> cached =
                (List<RecommendationResponse.RecommendedProduct>) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return RecommendationResponse.builder()
                    .type("TRENDING")
                    .products(cached)
                    .reason("Trending now")
                    .build();
        }

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Object[]> trending = behaviorRepository.findTrendingProducts(since, PageRequest.of(0, limit));

        List<RecommendationResponse.RecommendedProduct> recommendations = trending.stream()
                .map(arr -> RecommendationResponse.RecommendedProduct.builder()
                        .productId((Long) arr[0])
                        .score(((Number) arr[1]).doubleValue())
                        .reason("Trending")
                        .build())
                .collect(Collectors.toList());

        if (!recommendations.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, recommendations, Duration.ofMinutes(15));
        }

        return RecommendationResponse.builder()
                .type("TRENDING")
                .products(recommendations)
                .reason("Trending now")
                .build();
    }

    private RecommendationResponse getNewArrivals(int limit) {
        // In real implementation, fetch from product-service
        return RecommendationResponse.builder()
                .type("NEW_ARRIVALS")
                .products(new ArrayList<>())
                .reason("New arrivals")
                .build();
    }

    private List<RecommendationResponse.RecommendedProduct> buildPersonalizedList(
            List<Object[]> topCategories,
            List<Object[]> topBrands,
            Set<Long> excludeProductIds,
            int limit) {
        // Placeholder - in real implementation, query product-service with filters
        return new ArrayList<>();
    }

    public void trackBehavior(Long userId, Long productId, UserBehavior.BehaviorType behaviorType,
                              String searchQuery, Long categoryId, Long brandId) {
        Double weight = BEHAVIOR_WEIGHTS.getOrDefault(behaviorType, 1.0);

        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(behaviorType)
                .score(weight)
                .searchQuery(searchQuery)
                .categoryId(categoryId)
                .brandId(brandId)
                .build();

        behaviorRepository.save(behavior);

        // Invalidate user's personalized cache
        String cacheKey = RECOMMENDATION_CACHE_PREFIX + "personalized:" + userId;
        redisTemplate.delete(cacheKey);

        log.debug("Tracked behavior: userId={}, productId={}, type={}", userId, productId, behaviorType);
    }
}
