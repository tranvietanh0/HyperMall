package com.hypermall.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private Long sellerId;
    private String productName;
    private String variantName;
    private String thumbnail;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private Boolean selected;
}
