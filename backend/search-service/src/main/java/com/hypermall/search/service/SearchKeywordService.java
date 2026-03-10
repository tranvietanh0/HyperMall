package com.hypermall.search.service;

import com.hypermall.search.document.SearchKeyword;
import com.hypermall.search.dto.AutocompleteResponse;
import com.hypermall.search.dto.TrendingKeywordResponse;
import com.hypermall.search.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchKeywordService {

    private final SearchKeywordRepository searchKeywordRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TRENDING_KEY = "search:trending";
    private static final String HOURLY_TRENDING_KEY = "search:trending:hourly";

    public void trackSearch(String keyword) {
        String normalizedKeyword = keyword.toLowerCase().trim();

        // Update in Elasticsearch
        SearchKeyword searchKeyword = searchKeywordRepository.findByKeyword(normalizedKeyword)
                .orElse(SearchKeyword.builder()
                        .id(UUID.randomUUID().toString())
                        .keyword(normalizedKeyword)
                        .searchCount(0L)
                        .clickCount(0L)
                        .createdAt(LocalDateTime.now())
                        .build());

        searchKeyword.setSearchCount(searchKeyword.getSearchCount() + 1);
        searchKeyword.setLastSearchedAt(LocalDateTime.now());
        searchKeywordRepository.save(searchKeyword);

        // Update in Redis for real-time trending
        redisTemplate.opsForZSet().incrementScore(TRENDING_KEY, normalizedKeyword, 1);
        redisTemplate.opsForZSet().incrementScore(HOURLY_TRENDING_KEY, normalizedKeyword, 1);
    }

    public void trackClick(String keyword) {
        String normalizedKeyword = keyword.toLowerCase().trim();

        searchKeywordRepository.findByKeyword(normalizedKeyword).ifPresent(searchKeyword -> {
            searchKeyword.setClickCount(searchKeyword.getClickCount() + 1);
            searchKeywordRepository.save(searchKeyword);
        });
    }

    public List<TrendingKeywordResponse> getTrendingKeywords(int limit) {
        // Get from Redis first for real-time data
        Set<ZSetOperations.TypedTuple<String>> trending = redisTemplate.opsForZSet()
                .reverseRangeWithScores(TRENDING_KEY, 0, limit - 1);

        if (trending == null || trending.isEmpty()) {
            // Fallback to Elasticsearch
            return searchKeywordRepository.findAllByOrderBySearchCountDesc(PageRequest.of(0, limit))
                    .stream()
                    .map(sk -> TrendingKeywordResponse.builder()
                            .keyword(sk.getKeyword())
                            .searchCount(sk.getSearchCount())
                            .rank((long) (searchKeywordRepository.findAllByOrderBySearchCountDesc(PageRequest.of(0, limit))
                                    .indexOf(sk) + 1))
                            .trending(false)
                            .build())
                    .collect(Collectors.toList());
        }

        List<TrendingKeywordResponse> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : trending) {
            result.add(TrendingKeywordResponse.builder()
                    .keyword(tuple.getValue())
                    .searchCount(tuple.getScore() != null ? tuple.getScore().longValue() : 0)
                    .rank((long) rank++)
                    .trending(isKeywordTrending(tuple.getValue()))
                    .build());
        }

        return result;
    }

    public List<TrendingKeywordResponse> getHotKeywords(int limit) {
        Set<ZSetOperations.TypedTuple<String>> hotKeywords = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOURLY_TRENDING_KEY, 0, limit - 1);

        if (hotKeywords == null) {
            return Collections.emptyList();
        }

        List<TrendingKeywordResponse> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : hotKeywords) {
            result.add(TrendingKeywordResponse.builder()
                    .keyword(tuple.getValue())
                    .searchCount(tuple.getScore() != null ? tuple.getScore().longValue() : 0)
                    .rank((long) rank++)
                    .trending(true)
                    .build());
        }

        return result;
    }

    public List<AutocompleteResponse.SuggestionItem> getSuggestions(String query, int limit) {
        String normalizedQuery = query.toLowerCase().trim();

        return searchKeywordRepository
                .findByKeywordContainingIgnoreCaseOrderBySearchCountDesc(normalizedQuery, PageRequest.of(0, limit))
                .stream()
                .map(sk -> AutocompleteResponse.SuggestionItem.builder()
                        .text(sk.getKeyword())
                        .highlighted(highlightMatch(sk.getKeyword(), normalizedQuery))
                        .searchCount(sk.getSearchCount())
                        .build())
                .collect(Collectors.toList());
    }

    private String highlightMatch(String text, String query) {
        int index = text.toLowerCase().indexOf(query.toLowerCase());
        if (index == -1) {
            return text;
        }
        return text.substring(0, index) +
                "<em>" + text.substring(index, index + query.length()) + "</em>" +
                text.substring(index + query.length());
    }

    private boolean isKeywordTrending(String keyword) {
        Double hourlyScore = redisTemplate.opsForZSet().score(HOURLY_TRENDING_KEY, keyword);
        Double totalScore = redisTemplate.opsForZSet().score(TRENDING_KEY, keyword);

        if (hourlyScore == null || totalScore == null || totalScore == 0) {
            return false;
        }

        // A keyword is trending if hourly searches are more than 10% of total
        return hourlyScore / totalScore > 0.1;
    }

    // Reset hourly trending every hour
    @Scheduled(cron = "0 0 * * * *")
    public void resetHourlyTrending() {
        redisTemplate.delete(HOURLY_TRENDING_KEY);
        log.info("Hourly trending keywords reset");
    }

    // Sync Redis trending to Elasticsearch daily
    @Scheduled(cron = "0 0 0 * * *")
    public void syncTrendingToElasticsearch() {
        Set<ZSetOperations.TypedTuple<String>> trending = redisTemplate.opsForZSet()
                .reverseRangeWithScores(TRENDING_KEY, 0, 999);

        if (trending != null) {
            for (ZSetOperations.TypedTuple<String> tuple : trending) {
                String keyword = tuple.getValue();
                Long count = tuple.getScore() != null ? tuple.getScore().longValue() : 0;

                searchKeywordRepository.findByKeyword(keyword).ifPresentOrElse(
                        sk -> {
                            sk.setSearchCount(count);
                            searchKeywordRepository.save(sk);
                        },
                        () -> {
                            SearchKeyword newKeyword = SearchKeyword.builder()
                                    .id(UUID.randomUUID().toString())
                                    .keyword(keyword)
                                    .searchCount(count)
                                    .clickCount(0L)
                                    .createdAt(LocalDateTime.now())
                                    .lastSearchedAt(LocalDateTime.now())
                                    .build();
                            searchKeywordRepository.save(newKeyword);
                        }
                );
            }
            log.info("Synced {} trending keywords to Elasticsearch", trending.size());
        }
    }
}
