package com.hypermall.product.service;

import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.product.client.InternalSellerStatusResponse;
import com.hypermall.product.client.SellerStatusClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerGuardServiceTest {

    @Mock
    private SellerStatusClient sellerStatusClient;

    @InjectMocks
    private SellerGuardService sellerGuardService;

    @Test
    @DisplayName("Should allow active seller")
    void requireActiveSeller_WithActiveSeller_ShouldPass() {
        when(sellerStatusClient.getSellerStatusByUserId(10L)).thenReturn(
                new InternalSellerStatusResponse(1L, 10L, "alpha", "ACTIVE", true)
        );

        assertThatCode(() -> sellerGuardService.requireActiveSeller(10L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject pending seller")
    void requireActiveSeller_WithPendingSeller_ShouldThrowException() {
        when(sellerStatusClient.getSellerStatusByUserId(10L)).thenReturn(
                new InternalSellerStatusResponse(1L, 10L, "alpha", "PENDING", false)
        );

        assertThatThrownBy(() -> sellerGuardService.requireActiveSeller(10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Seller account is pending approval");
    }

    @Test
    @DisplayName("Should reject suspended seller")
    void requireActiveSeller_WithSuspendedSeller_ShouldThrowException() {
        when(sellerStatusClient.getSellerStatusByUserId(10L)).thenReturn(
                new InternalSellerStatusResponse(1L, 10L, "alpha", "SUSPENDED", false)
        );

        assertThatThrownBy(() -> sellerGuardService.requireActiveSeller(10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Seller account is suspended");
    }

    @Test
    @DisplayName("Should propagate missing seller profile")
    void requireActiveSeller_WithoutSellerProfile_ShouldThrowException() {
        when(sellerStatusClient.getSellerStatusByUserId(10L))
                .thenThrow(new ResourceNotFoundException("Seller profile not found for userId: 10"));

        assertThatThrownBy(() -> sellerGuardService.requireActiveSeller(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Seller profile not found for userId: 10");
    }
}
