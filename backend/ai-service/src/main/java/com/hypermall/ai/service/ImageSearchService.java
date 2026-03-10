package com.hypermall.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.ai.client.OpenAiClient;
import com.hypermall.ai.dto.ImageSearchRequest;
import com.hypermall.ai.dto.ImageSearchResponse;
import com.hypermall.ai.entity.ProductEmbedding;
import com.hypermall.ai.repository.ProductEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageSearchService {

    private final ProductEmbeddingRepository embeddingRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public ImageSearchResponse searchByImage(ImageSearchRequest request) {
        int limit = request.getLimit() != null ? request.getLimit() : 10;

        // Get embedding for the query image
        List<Float> queryEmbedding;
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            queryEmbedding = openAiClient.getImageEmbedding(request.getImageUrl());
        } else if (request.getImageBase64() != null) {
            // For base64, would need to upload to temporary storage first
            queryEmbedding = Collections.emptyList();
        } else {
            return ImageSearchResponse.builder()
                    .products(Collections.emptyList())
                    .build();
        }

        if (queryEmbedding.isEmpty()) {
            log.warn("Could not generate embedding for image");
            return ImageSearchResponse.builder()
                    .products(Collections.emptyList())
                    .build();
        }

        // Find similar products by cosine similarity
        List<ProductEmbedding> allEmbeddings = embeddingRepository.findAll();

        List<ImageSearchResponse.SimilarProduct> similarProducts = allEmbeddings.stream()
                .map(pe -> {
                    List<Float> productEmbedding = parseEmbedding(pe.getImageEmbedding());
                    if (productEmbedding.isEmpty()) {
                        productEmbedding = parseEmbedding(pe.getTextEmbedding());
                    }

                    double similarity = cosineSimilarity(queryEmbedding, productEmbedding);

                    return ImageSearchResponse.SimilarProduct.builder()
                            .productId(pe.getProductId())
                            .similarity(similarity)
                            .build();
                })
                .filter(sp -> sp.getSimilarity() > 0.5) // Threshold
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(limit)
                .collect(Collectors.toList());

        return ImageSearchResponse.builder()
                .products(similarProducts)
                .build();
    }

    public void indexProductEmbedding(Long productId, String productName, String description, String imageUrl) {
        // Generate text embedding from product name + description
        String text = productName + " " + (description != null ? description : "");
        List<Float> textEmbedding = openAiClient.getTextEmbedding(text);

        // Generate image embedding if image URL is available
        List<Float> imageEmbedding = Collections.emptyList();
        if (imageUrl != null && !imageUrl.isBlank()) {
            imageEmbedding = openAiClient.getImageEmbedding(imageUrl);
        }

        ProductEmbedding embedding = embeddingRepository.findByProductId(productId)
                .orElse(ProductEmbedding.builder().productId(productId).build());

        try {
            if (!textEmbedding.isEmpty()) {
                embedding.setTextEmbedding(objectMapper.writeValueAsString(textEmbedding));
            }
            if (!imageEmbedding.isEmpty()) {
                embedding.setImageEmbedding(objectMapper.writeValueAsString(imageEmbedding));
            }
            embedding.setImageUrl(imageUrl);
            embedding.setEmbeddingModel("text-embedding-3-small");
            embedding.setEmbeddingDimension(textEmbedding.isEmpty() ? 0 : textEmbedding.size());

            embeddingRepository.save(embedding);
            log.info("Indexed embedding for product: {}", productId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize embedding for product {}: {}", productId, e.getMessage());
        }
    }

    public void deleteProductEmbedding(Long productId) {
        embeddingRepository.deleteByProductId(productId);
        log.info("Deleted embedding for product: {}", productId);
    }

    private List<Float> parseEmbedding(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse embedding: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1.isEmpty() || vec2.isEmpty() || vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
