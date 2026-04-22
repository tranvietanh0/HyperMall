package com.hypermall.seller.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.seller.dto.request.CreateSellerRequest;
import com.hypermall.seller.dto.request.UpdateSellerRequest;
import com.hypermall.seller.dto.response.InternalSellerStatusResponse;
import com.hypermall.seller.dto.response.SellerDashboardResponse;
import com.hypermall.seller.dto.response.SellerResponse;
import com.hypermall.seller.entity.BusinessType;
import com.hypermall.seller.entity.Seller;
import com.hypermall.seller.entity.SellerStatus;
import com.hypermall.seller.mapper.SellerMapper;
import com.hypermall.seller.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private SellerMapper sellerMapper;

    @InjectMocks
    private SellerService sellerService;

    private Seller seller;
    private SellerResponse sellerResponse;
    private CreateSellerRequest createRequest;
    private UpdateSellerRequest updateRequest;

    @BeforeEach
    void setUp() {
        seller = Seller.builder()
                .id(10L)
                .userId(20L)
                .shopName("Alpha Store")
                .shopSlug("alpha-store")
                .description("Store description")
                .businessType(BusinessType.INDIVIDUAL)
                .status(SellerStatus.PENDING)
                .rating(0.0)
                .totalProducts(0)
                .totalFollowers(0)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now())
                .build();

        sellerResponse = new SellerResponse();
        sellerResponse.setId(seller.getId());
        sellerResponse.setUserId(seller.getUserId());
        sellerResponse.setShopName(seller.getShopName());
        sellerResponse.setShopSlug(seller.getShopSlug());
        sellerResponse.setStatus(seller.getStatus());

        createRequest = new CreateSellerRequest();
        createRequest.setShopName("Alpha Store");
        createRequest.setDescription("Store description");
        createRequest.setBusinessType(BusinessType.INDIVIDUAL);
        createRequest.setTaxCode("TAX-001");
        createRequest.setBankAccountNumber("123456789");
        createRequest.setBankName("VCB");
        createRequest.setBankAccountHolder("Alpha Owner");

        updateRequest = new UpdateSellerRequest();
        updateRequest.setShopName("Alpha Store Updated");
        updateRequest.setDescription("Updated description");
        updateRequest.setBusinessType(BusinessType.COMPANY);
        updateRequest.setTaxCode("TAX-002");
        updateRequest.setBankAccountNumber("987654321");
        updateRequest.setBankName("ACB");
        updateRequest.setBankAccountHolder("Updated Owner");
    }

    @Nested
    @DisplayName("Register seller")
    class RegisterSellerTests {

        @Test
        void shouldRegisterSellerSuccessfully() {
            when(sellerRepository.existsByUserId(20L)).thenReturn(false);
            when(sellerRepository.existsByShopSlug("alpha-store")).thenReturn(false);
            when(sellerRepository.save(any(Seller.class))).thenReturn(seller);
            when(sellerMapper.toSellerResponse(seller)).thenReturn(sellerResponse);

            SellerResponse response = sellerService.registerSeller(20L, createRequest);

            assertThat(response.getShopSlug()).isEqualTo("alpha-store");
            verify(sellerRepository).save(any(Seller.class));
        }

        @Test
        void shouldRejectDuplicateSellerProfile() {
            when(sellerRepository.existsByUserId(20L)).thenReturn(true);

            assertThatThrownBy(() -> sellerService.registerSeller(20L, createRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("User already has a seller profile");
        }

        @Test
        void shouldGenerateUniqueSlugWhenBaseSlugExists() {
            when(sellerRepository.existsByUserId(20L)).thenReturn(false);
            when(sellerRepository.existsByShopSlug(anyString())).thenReturn(true);
            when(sellerRepository.save(any(Seller.class))).thenReturn(seller);
            when(sellerMapper.toSellerResponse(seller)).thenReturn(sellerResponse);

            SellerResponse response = sellerService.registerSeller(20L, createRequest);

            assertThat(response).isNotNull();
            verify(sellerRepository).save(any(Seller.class));
        }
    }

    @Nested
    @DisplayName("Profile lookup")
    class ProfileLookupTests {

        @Test
        void shouldReturnCurrentSellerProfile() {
            when(sellerRepository.findByUserId(20L)).thenReturn(Optional.of(seller));
            when(sellerMapper.toSellerResponse(seller)).thenReturn(sellerResponse);

            SellerResponse response = sellerService.getMySellerProfile(20L);

            assertThat(response.getId()).isEqualTo(10L);
        }

        @Test
        void shouldReturnInternalSellerStatus() {
            when(sellerRepository.findByUserId(20L)).thenReturn(Optional.of(seller));

            InternalSellerStatusResponse response = sellerService.getInternalSellerStatusByUserId(20L);

            assertThat(response.getSellerId()).isEqualTo(10L);
            assertThat(response.getUserId()).isEqualTo(20L);
            assertThat(response.getStatus()).isEqualTo(SellerStatus.PENDING);
            assertThat(response.isActive()).isFalse();
        }

        @Test
        void shouldThrowWhenInternalSellerProfileMissing() {
            when(sellerRepository.findByUserId(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.getInternalSellerStatusByUserId(20L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Seller profile not found for userId: 20");
        }
    }

    @Nested
    @DisplayName("Seller status")
    class SellerStatusTests {

        @Test
        void shouldUpdateSellerStatus() {
            when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));
            when(sellerRepository.save(seller)).thenReturn(seller);
            when(sellerMapper.toSellerResponse(seller)).thenReturn(sellerResponse);

            SellerResponse response = sellerService.updateSellerStatus(10L, SellerStatus.ACTIVE);

            assertThat(response).isNotNull();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        void shouldRejectSameStatusUpdate() {
            when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));

            assertThatThrownBy(() -> sellerService.updateSellerStatus(10L, SellerStatus.PENDING))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Seller status is already PENDING");
        }
    }

    @Test
    void shouldUpdateSellerProfile() {
        when(sellerRepository.findByUserId(20L)).thenReturn(Optional.of(seller));
        when(sellerRepository.save(seller)).thenReturn(seller);
        when(sellerMapper.toSellerResponse(seller)).thenReturn(sellerResponse);

        SellerResponse response = sellerService.updateMySellerProfile(20L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(seller.getShopName()).isEqualTo("Alpha Store Updated");
        assertThat(seller.getBusinessType()).isEqualTo(BusinessType.COMPANY);
        assertThat(seller.getShopSlug()).isEqualTo("alpha-store");
    }

    @Test
    void shouldBuildDashboardFromSellerProfile() {
        when(sellerRepository.findByUserId(20L)).thenReturn(Optional.of(seller));

        SellerDashboardResponse response = sellerService.getMyDashboard(20L);

        assertThat(response.getSellerId()).isEqualTo(10L);
        assertThat(response.getShopSlug()).isEqualTo("alpha-store");
        assertThat(response.getStatus()).isEqualTo(SellerStatus.PENDING);
    }

    @Test
    void shouldGetSellersByStatus() {
        when(sellerRepository.findAllByStatusOrderByCreatedAtDesc(SellerStatus.PENDING)).thenReturn(List.of(seller));
        when(sellerMapper.toSellerResponseList(List.of(seller))).thenReturn(List.of(sellerResponse));

        List<SellerResponse> response = sellerService.getSellersByStatus(SellerStatus.PENDING);

        assertThat(response).hasSize(1);
    }
}
