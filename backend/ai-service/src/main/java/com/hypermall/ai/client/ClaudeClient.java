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
public class ClaudeClient implements AiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public ClaudeClient(
            @Value("${app.ai.claude.api-key:}") String apiKey,
            @Value("${app.ai.claude.base-url:https://api.anthropic.com/v1}") String baseUrl,
            @Value("${app.ai.claude.model:claude-sonnet-4-20250514}") String model,
            ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public String chat(List<Message> messages, String systemPrompt) {
        try {
            List<Map<String, String>> formattedMessages = messages.stream()
                    .map(m -> Map.of("role", m.role(), "content", m.content()))
                    .collect(Collectors.toList());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", formattedMessages);
            requestBody.put("max_tokens", 1000);

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                requestBody.put("system", systemPrompt);
            }

            String response = webClient.post()
                    .uri("/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            log.error("Claude chat error: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from Claude", e);
        }
    }

    @Override
    public List<Float> getTextEmbedding(String text) {
        // Claude doesn't have an embedding API
        log.warn("Claude text embedding not available");
        return Collections.emptyList();
    }

    @Override
    public List<Float> getImageEmbedding(String imageUrl) {
        log.warn("Claude image embedding not available");
        return Collections.emptyList();
    }

    @Override
    public String getProviderName() {
        return "claude";
    }
}
