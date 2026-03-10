package com.hypermall.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OpenAiClient implements AiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiClient(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String model,
            ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public String chat(List<Message> messages, String systemPrompt) {
        try {
            List<Map<String, String>> formattedMessages = new ArrayList<>();

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                formattedMessages.add(Map.of("role", "system", "content", systemPrompt));
            }

            formattedMessages.addAll(messages.stream()
                    .map(m -> Map.of("role", m.role(), "content", m.content()))
                    .collect(Collectors.toList()));

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", formattedMessages,
                    "temperature", 0.7,
                    "max_tokens", 1000
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("OpenAI chat error: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from OpenAI", e);
        }
    }

    @Override
    public List<Float> getTextEmbedding(String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "text-embedding-3-small",
                    "input", text
            );

            String response = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode embeddingNode = jsonNode.path("data").get(0).path("embedding");

            List<Float> embedding = new ArrayList<>();
            embeddingNode.forEach(node -> embedding.add((float) node.asDouble()));
            return embedding;

        } catch (Exception e) {
            log.error("OpenAI embedding error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Float> getImageEmbedding(String imageUrl) {
        // OpenAI doesn't have a direct image embedding API
        // Would need to use CLIP or similar
        log.warn("OpenAI image embedding not implemented");
        return Collections.emptyList();
    }

    @Override
    public String getProviderName() {
        return "openai";
    }
}
