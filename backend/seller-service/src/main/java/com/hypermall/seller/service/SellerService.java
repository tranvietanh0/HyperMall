package com.hypermall.seller.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.common.util.StringUtil;
import com.hypermall.seller.dto.request.CreateSellerRequest;
import com.hypermall.seller.dto.request.UpdateSellerRequest;
import com.hypermall.seller.dto.response.SellerDashboardResponse;
import com.hypermall.seller.dto.response.SellerResponse;
import com.hypermall.seller.entity.Seller;
import com.hypermall.seller.entity.SellerStatus;
import com.hypermall.seller.mapper.SellerMapper;
import com.hypermall.seller.repository.SellerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;

    @Transactional
    public SellerResponse registerSeller(Long userId, CreateSellerRequest request) {
        if (sellerRepository.existsByUserId(userId)) {
            throw new BadRequestException("User already has a seller profile");
        }

        String baseSlug = StringUtil.generateSlug(request.getShopName());
        String finalSlug = sellerRepository.existsByShopSlug(baseSlug)
                ? StringUtil.generateUniqueSlug(request.getShopName())
                : baseSlug;

        Seller seller = Seller.builder()
                .userId(userId)
                .shopName(request.getShopName())
                .shopSlug(finalSlug)
                .logo(request.getLogo())
                .banner(request.getBanner())
                .description(request.getDescription())
                .businessType(request.getBusinessType())
                .businessLicense(request.getBusinessLicense())
                .taxCode(request.getTaxCode())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankName(request.getBankName())
                .bankAccountHolder(request.getBankAccountHolder())
                .status(SellerStatus.PENDING)
                .rating(0.0)
                .totalProducts(0)
                .totalFollowers(0)
                .build();

        Seller saved = sellerRepository.save(seller);
        log.info("Seller profile created. userId={}, sellerId={}", userId, saved.getId());
        return sellerMapper.toSellerResponse(saved);
    }

    @Transactional(readOnly = true)
    public SellerResponse getMySellerProfile(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for current user"));
        return sellerMapper.toSellerResponse(seller);
    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse getMyDashboard(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for current user"));

        return SellerDashboardResponse.builder()
                .sellerId(seller.getId())
                .shopName(seller.getShopName())
                .shopSlug(seller.getShopSlug())
                .status(seller.getStatus())
                .rating(seller.getRating())
                .totalProducts(seller.getTotalProducts())
                .totalFollowers(seller.getTotalFollowers())
                .joinedAt(seller.getCreatedAt())
                .lastUpdatedAt(seller.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public SellerResponse getSellerById(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        return sellerMapper.toSellerResponse(seller);
    }

    @Transactional(readOnly = true)
    public SellerResponse getSellerBySlug(String slug) {
        Seller seller = sellerRepository.findByShopSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with slug: " + slug));
        return sellerMapper.toSellerResponse(seller);
    }

    @Transactional
    public SellerResponse updateMySellerProfile(Long userId, UpdateSellerRequest request) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for current user"));

        seller.setShopName(request.getShopName());
        seller.setLogo(request.getLogo());
        seller.setBanner(request.getBanner());
        seller.setDescription(request.getDescription());
        seller.setBusinessType(request.getBusinessType());
        seller.setBusinessLicense(request.getBusinessLicense());
        seller.setTaxCode(request.getTaxCode());
        seller.setBankAccountNumber(request.getBankAccountNumber());
        seller.setBankName(request.getBankName());
        seller.setBankAccountHolder(request.getBankAccountHolder());

        Seller updated = sellerRepository.save(seller);
        log.info("Seller profile updated. userId={}, sellerId={}", userId, updated.getId());
        return sellerMapper.toSellerResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<SellerResponse> getSellersByStatus(SellerStatus status) {
        return sellerMapper.toSellerResponseList(sellerRepository.findAllByStatusOrderByCreatedAtDesc(status));
    }

    @Transactional(readOnly = true)
    public Page<SellerResponse> searchSellers(SellerStatus status, String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return sellerRepository.searchSellers(status, normalizedKeyword, pageable)
                .map(sellerMapper::toSellerResponse);
    }

    @Transactional
    public SellerResponse updateSellerStatus(Long id, SellerStatus status) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setStatus(status);
        Seller updated = sellerRepository.save(seller);
        log.info("Seller status updated. sellerId={}, status={}", id, status);
        return sellerMapper.toSellerResponse(updated);
    }
}
