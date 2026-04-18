package com.hypermall.ai.service;

import com.hypermall.ai.client.CliProxyApiClient;
import com.hypermall.ai.config.AiProperties;
import com.hypermall.ai.dto.internal.CliProxyChatCompletionRequest;
import com.hypermall.ai.dto.request.ChatRequest;
import com.hypermall.ai.dto.response.ChatResponse;
import com.hypermall.ai.exception.AiServiceUnavailableException;
import com.hypermall.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private CliProxyApiClient cliProxyApiClient;

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private ProductGroundingService productGroundingService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        chatService = new ChatService(properties, cliProxyApiClient, promptBuilderService, productGroundingService);
    }

    @Test
    void returnsProviderResponseWhenAvailable() {
        ChatRequest request = ChatRequest.builder()
                .message("Find me a budget mouse")
                .build();

        ProductGroundingService.GroundingResult groundingResult = new ProductGroundingService.GroundingResult(
                null,
                List.of(ChatResponse.ProductSuggestion.builder()
                        .productId(7L)
                        .productName("Budget Mouse")
                        .price(199000.0)
                        .build())
        );

        when(productGroundingService.buildGrounding(request)).thenReturn(groundingResult);
        when(promptBuilderService.buildMessages(any(), any())).thenReturn(List.of(
                CliProxyChatCompletionRequest.Message.builder().role("user").content("prompt").build()
        ));
        when(cliProxyApiClient.createChatCompletion(any())).thenReturn("Here are three good choices.");

        ChatResponse response = chatService.chat(request);

        assertEquals("Here are three good choices.", response.getMessage());
        assertEquals(1, response.getProductSuggestions().size());
        assertTrue(!response.isDegraded());
    }

    @Test
    void returnsFallbackResponseWhenProviderFails() {
        ChatRequest request = ChatRequest.builder()
                .message("How do I track my order?")
                .build();

        when(productGroundingService.buildGrounding(request)).thenReturn(
                new ProductGroundingService.GroundingResult(null, List.of())
        );
        when(promptBuilderService.buildMessages(any(), any())).thenReturn(List.of());
        when(cliProxyApiClient.createChatCompletion(any())).thenThrow(new AiServiceUnavailableException("down"));

        ChatResponse response = chatService.chat(request);

        assertTrue(response.isDegraded());
        assertTrue(response.getMessage().contains("temporarily unavailable"));
        assertTrue(response.getSuggestedActions().stream().anyMatch(action -> "/profile".equals(action.getValue())));
    }

    @Test
    void rejectsOverlongHistory() {
        ChatRequest request = ChatRequest.builder()
                .message("hello")
                .history(List.of(
                        ChatRequest.ChatMessage.builder().role("user").content("1").build(),
                        ChatRequest.ChatMessage.builder().role("assistant").content("2").build(),
                        ChatRequest.ChatMessage.builder().role("user").content("3").build(),
                        ChatRequest.ChatMessage.builder().role("assistant").content("4").build(),
                        ChatRequest.ChatMessage.builder().role("user").content("5").build(),
                        ChatRequest.ChatMessage.builder().role("assistant").content("6").build(),
                        ChatRequest.ChatMessage.builder().role("user").content("7").build()
                ))
                .build();

        assertThrows(BadRequestException.class, () -> chatService.chat(request));
    }
}
