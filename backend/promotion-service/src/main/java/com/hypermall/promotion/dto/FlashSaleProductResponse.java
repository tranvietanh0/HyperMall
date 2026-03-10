package com.hypermall.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlashSaleProductResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal flashSalePrice;
    private int discountPercent;
    private Integer stockLimit;
    private Integer soldCount;
    private Integer remainingStock;
    private boolean available;
}
