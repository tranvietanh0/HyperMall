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

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CliProxyApiClient {

    private final WebClient cliProxyWebClient;
    private final AiProperties aiProperties;

    public String createChatCompletion(List<CliProxyChatCompletionRequest.Message> messages) {
        CliProxyChatCompletionRequest request = CliProxyChatCompletionRequest.builder()
                .model(aiProperties.getProvider().getModel())
                .messages(messages)
                .temperature(0.3)
                .stream(false)
                .build();

        try {
            CliProxyChatCompletionResponse response = cliProxyWebClient.post()
                    .uri("/v1/chat/completions")
                    .headers(headers -> applyAuthHeader(headers, aiProperties.getProvider().getApiKey()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CliProxyChatCompletionResponse.class)
                    .timeout(Duration.ofMillis(aiProperties.getProvider().getTimeoutMs()))
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
        } catch (Exception ex) {
            log.warn("CLIProxyAPI request failed: {}", ex.getMessage());
            throw new AiServiceUnavailableException("AI assistant is temporarily unavailable", ex);
        }
    }

    private void applyAuthHeader(HttpHeaders headers, String apiKey) {
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
    }
}
