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
public class GeminiClient implements AiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String apiKey;

    public GeminiClient(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ai.gemini.model:gemini-1.5-flash}") String model,
            ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String chat(List<Message> messages, String systemPrompt) {
        try {
            List<Map<String, Object>> contents = new ArrayList<>();

            // Add system prompt as first user message if provided
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                contents.add(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", "System instructions: " + systemPrompt))
                ));
                contents.add(Map.of(
                        "role", "model",
                        "parts", List.of(Map.of("text", "Understood. I will follow these instructions."))
                ));
            }

            // Add conversation messages
            for (Message msg : messages) {
                String role = msg.role().equals("assistant") ? "model" : "user";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", msg.content()))
                ));
            }

            Map<String, Object> requestBody = Map.of("contents", contents);

            String response = webClient.post()
                    .uri("/models/" + model + ":generateContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            log.error("Gemini chat error: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from Gemini", e);
        }
    }

    @Override
    public List<Float> getTextEmbedding(String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "content", Map.of("parts", List.of(Map.of("text", text)))
            );

            String response = webClient.post()
                    .uri("/models/embedding-001:embedContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode embeddingNode = jsonNode.path("embedding").path("values");

            List<Float> embedding = new ArrayList<>();
            embeddingNode.forEach(node -> embedding.add((float) node.asDouble()));
            return embedding;

        } catch (Exception e) {
            log.error("Gemini embedding error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Float> getImageEmbedding(String imageUrl) {
        // Would need to use Gemini's multimodal capabilities
        log.warn("Gemini image embedding not implemented yet");
        return Collections.emptyList();
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }
}
