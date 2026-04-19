package com.hypermall.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = true;
    private Provider provider = new Provider();
    private Provider fallbackProvider = new Provider();
    private Chat chat = new Chat();
    private Product product = new Product();

    @Data
    public static class Provider {
        private String baseUrl = "http://localhost:8317";
        private String apiKey;
        private String model = "gpt-4o-mini";
        private int timeoutMs = 15000;
        private boolean enabled = true;
    }

    @Data
    public static class Chat {
        private int maxMessageChars = 2000;
        private int maxHistoryMessages = 6;
        private int productSuggestionLimit = 3;
    }

    @Data
    public static class Product {
        private String serviceBaseUrl = "http://localhost:8082";
    }
}
