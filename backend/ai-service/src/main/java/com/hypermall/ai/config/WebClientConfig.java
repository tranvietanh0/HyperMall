package com.hypermall.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient cliProxyWebClient(WebClient.Builder builder, AiProperties aiProperties) {
        return builder
                .baseUrl(aiProperties.getProvider().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient productServiceWebClient(WebClient.Builder builder, AiProperties aiProperties) {
        return builder
                .baseUrl(aiProperties.getProduct().getServiceBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
