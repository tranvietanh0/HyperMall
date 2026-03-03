package com.hypermall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String thumbnail;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
