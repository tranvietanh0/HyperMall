package com.hypermall.promotion.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.promotion.dto.*;
import com.hypermall.promotion.entity.*;
import com.hypermall.promotion.mapper.PromotionMapper;
import com.hypermall.promotion.repository.FlashSaleProductRepository;
import com.hypermall.promotion.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final PromotionMapper promotionMapper;

    @Transactional
    public FlashSaleResponse createFlashSale(CreateFlashSaleRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        FlashSale flashSale = FlashSale.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bannerImage(request.getBannerImage())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(FlashSaleStatus.SCHEDULED)
                .build();

        // Add products
        for (CreateFlashSaleRequest.FlashSaleProductRequest productReq : request.getProducts()) {
            if (productReq.getFlashSalePrice().compareTo(productReq.getOriginalPrice()) >= 0) {
                throw new BadRequestException("Flash sale price must be less than original price");
            }

            FlashSaleProduct product = FlashSaleProduct.builder()
                    .flashSale(flashSale)
                    .productId(productReq.getProductId())
                    .variantId(productReq.getVariantId())
                    .productName(productReq.getProductName())
                    .productImage(productReq.getProductImage())
                    .originalPrice(productReq.getOriginalPrice())
                    .flashSalePrice(productReq.getFlashSalePrice())
                    .stockLimit(productReq.getStockLimit())
                    .sortOrder(productReq.getSortOrder() != null ? productReq.getSortOrder() : 0)
                    .build();
            flashSale.getProducts().add(product);
        }

        FlashSale saved = flashSaleRepository.save(flashSale);
        log.info("Flash sale created: {} ({})", saved.getName(), saved.getId());
        return promotionMapper.toFlashSaleResponse(saved);
    }

    @Transactional(readOnly = true)
    public FlashSaleResponse getFlashSaleById(Long id) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + id));
        return promotionMapper.toFlashSaleResponse(flashSale);
    }

    @Transactional(readOnly = true)
    public Page<FlashSaleResponse> getFlashSales(Pageable pageable) {
        return flashSaleRepository.findAll(pageable).map(promotionMapper::toFlashSaleResponse);
    }

    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getActiveFlashSales() {
        List<FlashSale> flashSales = flashSaleRepository.findActiveFlashSales(LocalDateTime.now());
        return promotionMapper.toFlashSaleResponseList(flashSales);
    }

    @Transactional(readOnly = true)
    public Optional<FlashSaleResponse> getCurrentFlashSale() {
        return flashSaleRepository.findCurrentFlashSale(LocalDateTime.now())
                .map(promotionMapper::toFlashSaleResponse);
    }

    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getUpcomingFlashSales() {
        List<FlashSale> flashSales = flashSaleRepository.findUpcomingFlashSales(LocalDateTime.now());
        return promotionMapper.toFlashSaleResponseList(flashSales);
    }

    @Transactional(readOnly = true)
    public Optional<FlashSaleProductResponse> getActiveFlashSaleProduct(Long productId) {
        return flashSaleProductRepository.findActiveFlashSaleProduct(productId)
                .map(promotionMapper::toFlashSaleProductResponse);
    }

    @Transactional
    public boolean purchaseFlashSaleProduct(Long flashSaleProductId, int quantity) {
        int updated = flashSaleProductRepository.incrementSoldCount(flashSaleProductId, quantity);
        if (updated > 0) {
            log.info("Flash sale product {} sold: {} units", flashSaleProductId, quantity);
            return true;
        }
        log.warn("Flash sale product {} purchase failed - insufficient stock", flashSaleProductId);
        return false;
    }

    @Transactional
    public FlashSaleResponse updateFlashSaleStatus(Long id, FlashSaleStatus status) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flash sale not found with id: " + id));
        flashSale.setStatus(status);
        return promotionMapper.toFlashSaleResponse(flashSaleRepository.save(flashSale));
    }

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void updateFlashSaleStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Activate scheduled flash sales
        List<FlashSale> toActivate = flashSaleRepository.findFlashSalesToActivate(now);
        for (FlashSale fs : toActivate) {
            fs.setStatus(FlashSaleStatus.ACTIVE);
            flashSaleRepository.save(fs);
            log.info("Flash sale activated: {} ({})", fs.getName(), fs.getId());
        }

        // End active flash sales
        List<FlashSale> toEnd = flashSaleRepository.findFlashSalesToEnd(now);
        for (FlashSale fs : toEnd) {
            fs.setStatus(FlashSaleStatus.ENDED);
            flashSaleRepository.save(fs);
            log.info("Flash sale ended: {} ({})", fs.getName(), fs.getId());
        }
    }
}
