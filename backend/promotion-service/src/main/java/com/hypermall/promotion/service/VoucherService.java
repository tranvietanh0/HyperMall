package com.hypermall.promotion.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ConflictException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.promotion.dto.*;
import com.hypermall.promotion.entity.*;
import com.hypermall.promotion.mapper.PromotionMapper;
import com.hypermall.promotion.repository.UserVoucherRepository;
import com.hypermall.promotion.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final PromotionMapper promotionMapper;

    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Voucher code already exists: " + request.getCode());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Voucher voucher = promotionMapper.toVoucher(request);
        Voucher saved = voucherRepository.save(voucher);
        log.info("Voucher created: {} ({})", saved.getCode(), saved.getId());
        return promotionMapper.toVoucherResponse(saved);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
        return promotionMapper.toVoucherResponse(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));
        return promotionMapper.toVoucherResponse(voucher);
    }

    @Transactional(readOnly = true)
    public Page<VoucherResponse> getVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable).map(promotionMapper::toVoucherResponse);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAvailableVouchers(Long sellerId) {
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(LocalDateTime.now(), sellerId);
        return promotionMapper.toVoucherResponseList(vouchers);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getUserVouchers(Long userId) {
        List<UserVoucher> userVouchers = userVoucherRepository.findByUserIdAndIsUsed(userId, false);
        return userVouchers.stream()
                .map(uv -> {
                    VoucherResponse response = promotionMapper.toVoucherResponse(uv.getVoucher());
                    response.setClaimed(true);
                    response.setUsed(uv.isUsed());
                    return response;
                })
                .toList();
    }

    @Transactional
    public VoucherResponse claimVoucher(Long userId, String code) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));

        if (!voucher.isValid()) {
            throw new BadRequestException("Voucher is not valid or has expired");
        }

        long userClaimCount = userVoucherRepository.countByUserIdAndVoucherId(userId, voucher.getId());
        if (userClaimCount >= voucher.getUserLimit()) {
            throw new BadRequestException("You have already claimed this voucher");
        }

        UserVoucher userVoucher = UserVoucher.builder()
                .userId(userId)
                .voucher(voucher)
                .build();
        userVoucherRepository.save(userVoucher);

        log.info("User {} claimed voucher {}", userId, code);

        VoucherResponse response = promotionMapper.toVoucherResponse(voucher);
        response.setClaimed(true);
        response.setUsed(false);
        return response;
    }

    @Transactional
    public ApplyVoucherResponse applyVoucher(Long userId, ApplyVoucherRequest request) {
        Voucher voucher = voucherRepository.findByCode(request.getCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + request.getCode()));

        // Validate voucher
        if (!voucher.isValid()) {
            return ApplyVoucherResponse.builder()
                    .code(request.getCode())
                    .valid(false)
                    .message("Voucher is not valid or has expired")
                    .build();
        }

        // Check min order value
        if (voucher.getMinOrderValue() != null &&
                request.getOrderAmount().compareTo(voucher.getMinOrderValue()) < 0) {
            return ApplyVoucherResponse.builder()
                    .code(request.getCode())
                    .valid(false)
                    .message("Order amount does not meet minimum requirement of " + voucher.getMinOrderValue())
                    .build();
        }

        // Check user limit
        long userUsageCount = userVoucherRepository.countByUserIdAndVoucherId(userId, voucher.getId());
        if (userUsageCount >= voucher.getUserLimit()) {
            return ApplyVoucherResponse.builder()
                    .code(request.getCode())
                    .valid(false)
                    .message("You have reached the usage limit for this voucher")
                    .build();
        }

        // Calculate discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;

        if (voucher.getType() == VoucherType.FREE_SHIPPING) {
            shippingDiscount = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        } else {
            discountAmount = voucher.calculateDiscount(request.getOrderAmount());
        }

        return ApplyVoucherResponse.builder()
                .code(voucher.getCode())
                .name(voucher.getName())
                .type(voucher.getType())
                .discountAmount(discountAmount)
                .shippingDiscount(shippingDiscount)
                .totalDiscount(discountAmount.add(shippingDiscount))
                .valid(true)
                .message("Voucher applied successfully")
                .build();
    }

    @Transactional
    public void useVoucher(Long userId, String code, Long orderId) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        // Find or create user voucher record
        UserVoucher userVoucher = userVoucherRepository.findUnusedByUserIdAndVoucherId(userId, voucher.getId())
                .orElseGet(() -> UserVoucher.builder()
                        .userId(userId)
                        .voucher(voucher)
                        .build());

        userVoucher.setOrderId(orderId);
        userVoucher.setUsed(true);
        userVoucher.setUsedAt(LocalDateTime.now());
        userVoucherRepository.save(userVoucher);

        // Increment usage count
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        log.info("Voucher {} used by user {} for order {}", code, userId, orderId);
    }

    @Transactional
    public VoucherResponse updateVoucherStatus(Long id, VoucherStatus status) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
        voucher.setStatus(status);
        return promotionMapper.toVoucherResponse(voucherRepository.save(voucher));
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void expireVouchers() {
        List<Voucher> expiredVouchers = voucherRepository.findExpiredVouchers(LocalDateTime.now());
        for (Voucher voucher : expiredVouchers) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            voucherRepository.save(voucher);
            log.info("Voucher expired: {}", voucher.getCode());
        }
    }
}
