package com.hypermall.promotion.mapper;

import com.hypermall.promotion.dto.*;
import com.hypermall.promotion.entity.*;
import org.mapstruct.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {

    @Mapping(target = "valid", expression = "java(voucher.isValid())")
    @Mapping(target = "claimed", ignore = true)
    @Mapping(target = "used", ignore = true)
    VoucherResponse toVoucherResponse(Voucher voucher);

    List<VoucherResponse> toVoucherResponseList(List<Voucher> vouchers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", constant = "0")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "userVouchers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "applicableCategories", expression = "java(listToCommaSeparated(request.getApplicableCategories()))")
    @Mapping(target = "applicableProducts", expression = "java(listToCommaSeparated(request.getApplicableProducts()))")
    Voucher toVoucher(CreateVoucherRequest request);

    @Mapping(target = "active", expression = "java(flashSale.isActive())")
    @Mapping(target = "upcoming", expression = "java(flashSale.isUpcoming())")
    @Mapping(target = "remainingSeconds", expression = "java(calculateRemainingSeconds(flashSale))")
    FlashSaleResponse toFlashSaleResponse(FlashSale flashSale);

    List<FlashSaleResponse> toFlashSaleResponseList(List<FlashSale> flashSales);

    @Mapping(target = "discountPercent", expression = "java(product.getDiscountPercent())")
    @Mapping(target = "remainingStock", expression = "java(product.getRemainingStock())")
    @Mapping(target = "available", expression = "java(product.isAvailable())")
    FlashSaleProductResponse toFlashSaleProductResponse(FlashSaleProduct product);

    List<FlashSaleProductResponse> toFlashSaleProductResponseList(List<FlashSaleProduct> products);

    default String listToCommaSeparated(List<Long> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null);
    }

    default long calculateRemainingSeconds(FlashSale flashSale) {
        if (flashSale == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (flashSale.isActive()) {
            return Duration.between(now, flashSale.getEndTime()).getSeconds();
        } else if (flashSale.isUpcoming()) {
            return Duration.between(now, flashSale.getStartTime()).getSeconds();
        }
        return 0;
    }
}
