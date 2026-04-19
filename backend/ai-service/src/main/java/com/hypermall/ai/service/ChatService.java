package com.hypermall.ai.service;

import com.hypermall.ai.client.CliProxyApiClient;
import com.hypermall.ai.config.AiProperties;
import com.hypermall.ai.dto.internal.CliProxyChatCompletionRequest;
import com.hypermall.ai.dto.request.ChatRequest;
import com.hypermall.ai.dto.response.ChatResponse;
import com.hypermall.ai.exception.AiServiceUnavailableException;
import com.hypermall.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiProperties aiProperties;
    private final CliProxyApiClient cliProxyApiClient;
    private final PromptBuilderService promptBuilderService;
    private final ProductGroundingService productGroundingService;

    public ChatResponse chat(ChatRequest request) {
        validateRequest(request);

        String sessionId = StringUtils.hasText(request.getSessionId())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        ProductGroundingService.GroundingResult groundingResult = productGroundingService.buildGrounding(request);
        List<ChatResponse.SuggestedAction> suggestedActions = buildSuggestedActions(request, groundingResult);

        if (!aiProperties.isEnabled()) {
            return buildFallbackResponse(sessionId, groundingResult, suggestedActions);
        }

        List<CliProxyChatCompletionRequest.Message> messages = promptBuilderService.buildMessages(request, groundingResult);

        try {
            String answer = createChatCompletionWithFallback(messages);
            return ChatResponse.builder()
                    .message(answer)
                    .sessionId(sessionId)
                    .suggestedActions(suggestedActions)
                    .productSuggestions(groundingResult.productSuggestions())
                    .degraded(false)
                    .build();
        } catch (AiServiceUnavailableException ex) {
            log.warn("Returning degraded AI chat response: {}", ex.getMessage());
            return buildFallbackResponse(sessionId, groundingResult, suggestedActions);
        }
    }

    private String createChatCompletionWithFallback(List<CliProxyChatCompletionRequest.Message> messages) {
        AiServiceUnavailableException primaryFailure = null;

        try {
            return cliProxyApiClient.createChatCompletion(messages, aiProperties.getProvider(), "Primary AI provider");
        } catch (AiServiceUnavailableException ex) {
            primaryFailure = ex;
            log.warn("Primary AI provider failed, attempting fallback: {}", ex.getMessage());
        }

        if (isFallbackProviderConfigured()) {
            try {
                return cliProxyApiClient.createChatCompletion(messages, aiProperties.getFallbackProvider(), "Fallback AI provider");
            } catch (AiServiceUnavailableException fallbackFailure) {
                log.warn("Fallback AI provider failed after primary provider: {}", fallbackFailure.getMessage());
                throw fallbackFailure;
            }
        }

        throw primaryFailure != null ? primaryFailure : new AiServiceUnavailableException("AI assistant is temporarily unavailable");
    }

    private boolean isFallbackProviderConfigured() {
        AiProperties.Provider fallbackProvider = aiProperties.getFallbackProvider();
        return fallbackProvider != null
                && fallbackProvider.isEnabled()
                && StringUtils.hasText(fallbackProvider.getBaseUrl())
                && StringUtils.hasText(fallbackProvider.getModel());
    }

    private void validateRequest(ChatRequest request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        if (!StringUtils.hasText(message)) {
            throw new BadRequestException("Message is required");
        }

        if (message.length() > aiProperties.getChat().getMaxMessageChars()) {
            throw new BadRequestException("Message is too long");
        }

        if (request.getHistory() != null && request.getHistory().size() > aiProperties.getChat().getMaxHistoryMessages()) {
            throw new BadRequestException("Conversation history exceeds the allowed limit");
        }

        if (request.getHistory() != null) {
            int totalHistoryCharacters = 0;
            for (ChatRequest.ChatMessage historyItem : request.getHistory()) {
                if (historyItem == null || !StringUtils.hasText(historyItem.getContent())) {
                    throw new BadRequestException("Conversation history contains an empty message");
                }

                String role = historyItem.getRole() != null ? historyItem.getRole().trim().toLowerCase(Locale.ROOT) : "";
                if (!"user".equals(role) && !"assistant".equals(role)) {
                    throw new BadRequestException("Conversation history contains an invalid role");
                }

                String content = historyItem.getContent().trim();
                if (content.length() > aiProperties.getChat().getMaxMessageChars()) {
                    throw new BadRequestException("Conversation history message is too long");
                }

                totalHistoryCharacters += content.length();
            }

            int maxHistoryCharacters = aiProperties.getChat().getMaxMessageChars() * aiProperties.getChat().getMaxHistoryMessages();
            if (totalHistoryCharacters > maxHistoryCharacters) {
                throw new BadRequestException("Conversation history exceeds the allowed size");
            }
        }
    }

    private ChatResponse buildFallbackResponse(
            String sessionId,
            ProductGroundingService.GroundingResult groundingResult,
            List<ChatResponse.SuggestedAction> suggestedActions
    ) {
        String fallback = !groundingResult.productSuggestions().isEmpty()
                ? "AI assistant is temporarily unavailable. Here are a few matching products you can explore while I reconnect."
                : "AI assistant is temporarily unavailable. Try searching for a product, browsing categories, or opening the current product page for more details.";

        return ChatResponse.builder()
                .message(fallback)
                .sessionId(sessionId)
                .suggestedActions(suggestedActions)
                .productSuggestions(groundingResult.productSuggestions())
                .degraded(true)
                .build();
    }

    private List<ChatResponse.SuggestedAction> buildSuggestedActions(
            ChatRequest request,
            ProductGroundingService.GroundingResult groundingResult
    ) {
        List<ChatResponse.SuggestedAction> actions = new ArrayList<>();
        String message = request.getMessage().trim();
        String normalized = message.toLowerCase(Locale.ROOT);

        if (!groundingResult.productSuggestions().isEmpty()) {
            actions.add(action("search", "Search related products", message));
        }

        if (normalized.contains("order") || normalized.contains("track") || normalized.contains("đơn") || normalized.contains("theo dõi")) {
            actions.add(action("link", "Track your order", "/profile"));
        }

        if (groundingResult.currentProduct() != null) {
            actions.add(action("link", "View current product", "/products/" + groundingResult.currentProduct().getId()));
        }

        actions.add(action("link", "Browse catalog", "/products"));

        if (actions.size() > 3) {
            return actions.subList(0, 3);
        }

        return actions;
    }

    private ChatResponse.SuggestedAction action(String type, String label, String value) {
        return ChatResponse.SuggestedAction.builder()
                .type(type)
                .label(label)
                .value(value)
                .build();
    }
}
