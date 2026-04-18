package com.hypermall.ai.service;

import com.hypermall.ai.dto.internal.CliProxyChatCompletionRequest;
import com.hypermall.ai.dto.request.ChatRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptBuilderService {

    public List<CliProxyChatCompletionRequest.Message> buildMessages(
            ChatRequest request,
            ProductGroundingService.GroundingResult groundingResult
    ) {
        List<CliProxyChatCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(systemMessage());

        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage item : request.getHistory()) {
                String normalizedRole = normalizeRole(item);
                if (normalizedRole != null && item != null && StringUtils.hasText(item.getContent())) {
                    messages.add(CliProxyChatCompletionRequest.Message.builder()
                            .role(normalizedRole)
                            .content(item.getContent().trim())
                            .build());
                }
            }
        }

        messages.add(CliProxyChatCompletionRequest.Message.builder()
                .role("user")
                .content(buildUserPrompt(request, groundingResult))
                .build());

        return messages;
    }

    private CliProxyChatCompletionRequest.Message systemMessage() {
        return CliProxyChatCompletionRequest.Message.builder()
                .role("system")
                .content("You are HyperMall shopping assistant. Help shoppers discover products, understand product pages, and navigate the store. Keep answers concise and practical. Never invent prices, stock, delivery times, or policy details when data is missing. If you are unsure, say so and suggest a search or category action. Never ask for passwords, OTPs, card numbers, or secrets.")
                .build();
    }

    private String buildUserPrompt(ChatRequest request, ProductGroundingService.GroundingResult groundingResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Shopper message: ").append(request.getMessage().trim()).append("\n\n");

        ChatRequest.ChatContext context = request.getContext();
        if (context != null) {
            if (StringUtils.hasText(context.getPageType())) {
                prompt.append("Page type: ").append(context.getPageType()).append("\n");
            }
            if (StringUtils.hasText(context.getPath())) {
                prompt.append("Path: ").append(context.getPath()).append("\n");
            }
            if (StringUtils.hasText(context.getProductName())) {
                prompt.append("Current product name hint: ").append(context.getProductName()).append("\n");
            }
        }

        ProductGroundingService.ProductSnapshot currentProduct = groundingResult.currentProduct();
        if (currentProduct != null) {
            prompt.append("\nCurrent product context:\n")
                    .append("- Name: ").append(currentProduct.getName()).append("\n")
                    .append("- Category: ").append(nullSafe(currentProduct.getCategoryName())).append("\n")
                    .append("- Price: ").append(nullSafe(currentProduct.getPrice())).append("\n")
                    .append("- Rating: ").append(nullSafe(currentProduct.getAvgRating())).append("\n")
                    .append("- Short description: ").append(nullSafe(currentProduct.getShortDescription())).append("\n");
        }

        if (!groundingResult.productSuggestions().isEmpty()) {
            prompt.append("\nCandidate products:\n");
            for (int i = 0; i < groundingResult.productSuggestions().size(); i++) {
                var suggestion = groundingResult.productSuggestions().get(i);
                prompt.append(i + 1)
                        .append(". ")
                        .append(suggestion.getProductName())
                        .append(" | price=")
                        .append(nullSafe(suggestion.getPrice()))
                        .append(" | productId=")
                        .append(suggestion.getProductId())
                        .append("\n");
            }
        }

        prompt.append("\nRespond in a friendly shopping assistant tone. If useful, mention up to 3 product suggestions already provided in context. Do not mention internal systems, APIs, or CLIProxyAPI.");
        return prompt.toString();
    }

    private String nullSafe(Object value) {
        return value != null ? value.toString() : "unknown";
    }

    private String normalizeRole(ChatRequest.ChatMessage item) {
        if (item == null || !StringUtils.hasText(item.getRole())) {
            return null;
        }

        String normalizedRole = item.getRole().trim().toLowerCase();
        if (!"user".equals(normalizedRole) && !"assistant".equals(normalizedRole)) {
            return null;
        }

        return normalizedRole;
    }
}
