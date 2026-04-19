package com.hypermall.ai.client;

import com.hypermall.ai.config.AiProperties;
import com.hypermall.ai.dto.internal.CliProxyChatCompletionRequest;
import com.hypermall.ai.dto.internal.CliProxyChatCompletionResponse;
import com.hypermall.ai.exception.AiServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CliProxyApiClient {

    private final WebClient.Builder webClientBuilder;

    public String createChatCompletion(List<CliProxyChatCompletionRequest.Message> messages, AiProperties.Provider provider, String providerName) {
        CliProxyChatCompletionRequest request = CliProxyChatCompletionRequest.builder()
                .model(provider.getModel())
                .messages(messages)
                .temperature(0.3)
                .stream(false)
                .build();

        WebClient webClient = webClientBuilder
                .baseUrl(provider.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            CliProxyChatCompletionResponse response = webClient.post()
                    .uri("/v1/chat/completions")
                    .headers(headers -> applyAuthHeader(headers, provider.getApiKey()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CliProxyChatCompletionResponse.class)
                    .timeout(Duration.ofMillis(provider.getTimeoutMs()))
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new AiServiceUnavailableException("AI provider returned an empty response");
            }

            CliProxyChatCompletionResponse.Message message = response.getChoices().get(0).getMessage();
            if (message == null || !StringUtils.hasText(message.getContent())) {
                throw new AiServiceUnavailableException("AI provider response did not include content");
            }

            return message.getContent().trim();
        } catch (AiServiceUnavailableException ex) {
            throw ex;
        } catch (WebClientResponseException ex) {
            log.warn("{} request failed with status {}: {}", providerName, ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new AiServiceUnavailableException(providerName + " is temporarily unavailable", ex);
        } catch (Exception ex) {
            log.warn("{} request failed: {}", providerName, ex.getMessage());
            throw new AiServiceUnavailableException(providerName + " is temporarily unavailable", ex);
        }
    }

    private void applyAuthHeader(HttpHeaders headers, String apiKey) {
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
    }
}
