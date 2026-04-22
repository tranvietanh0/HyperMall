package com.hypermall.product.client;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class SellerStatusClient {

    private static final ParameterizedTypeReference<ApiResponse<InternalSellerStatusResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient sellerRestClient;

    @Value("${app.seller-service.internal-token}")
    private String internalToken;

    public InternalSellerStatusResponse getSellerStatusByUserId(Long userId) {
        try {
            ApiResponse<InternalSellerStatusResponse> response = sellerRestClient.get()
                    .uri("/api/internal/sellers/users/{userId}", userId)
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .body(RESPONSE_TYPE);

            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Seller profile not found for userId: " + userId);
            }

            return response.getData();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Seller profile not found for userId: " + userId);
            }
            if (exception.getStatusCode() == HttpStatus.FORBIDDEN || exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ForbiddenException("Unable to verify seller status");
            }
            throw new BadRequestException("Unable to verify seller status");
        } catch (RestClientException exception) {
            throw new BadRequestException("Unable to verify seller status");
        }
    }
}
