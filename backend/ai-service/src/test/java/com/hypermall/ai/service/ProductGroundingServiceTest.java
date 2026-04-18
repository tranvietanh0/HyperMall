package com.hypermall.ai.service;

import com.hypermall.ai.config.AiProperties;
import com.hypermall.ai.dto.request.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductGroundingServiceTest {

    private ProductGroundingService productGroundingService;

    @BeforeEach
    void setUp() {
        productGroundingService = new ProductGroundingService(
                WebClient.builder().baseUrl("http://localhost:8082").build(),
                new AiProperties()
        );
    }

    @Test
    void detectsProductDiscoveryIntentForShoppingQueries() {
        assertTrue(productGroundingService.isProductDiscoveryIntent("Gợi ý tai nghe không dây rẻ hơn sản phẩm này"));
        assertTrue(productGroundingService.isProductDiscoveryIntent("Find me a budget gaming mouse"));
    }

    @Test
    void ignoresNonProductHelpQueries() {
        assertFalse(productGroundingService.isProductDiscoveryIntent("How do I track my order?"));
        assertFalse(productGroundingService.isProductDiscoveryIntent("Làm sao để đổi mật khẩu?"));
    }

    @Test
    void usesCurrentProductNameOnlyForComparativeQueries() {
        ChatRequest request = ChatRequest.builder()
                .message("Gợi ý mẫu rẻ hơn sản phẩm này")
                .context(ChatRequest.ChatContext.builder()
                        .productName("Sony WH-1000XM5")
                        .build())
                .build();

        assertTrue(productGroundingService.resolveKeyword(request).contains("Sony WH-1000XM5"));
        assertFalse(productGroundingService.resolveKeyword(
                ChatRequest.builder()
                        .message("Find me a budget gaming mouse")
                        .context(ChatRequest.ChatContext.builder().productName("Sony WH-1000XM5").build())
                        .build()
        ).contains("Sony WH-1000XM5"));
    }
}
