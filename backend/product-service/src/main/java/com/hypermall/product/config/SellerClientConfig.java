package com.hypermall.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SellerClientConfig {

    @Bean
    public RestClient sellerRestClient(
            RestClient.Builder builder,
            @Value("${app.seller-service.base-url}") String sellerServiceBaseUrl
    ) {
        return builder
                .baseUrl(sellerServiceBaseUrl)
                .build();
    }
}
