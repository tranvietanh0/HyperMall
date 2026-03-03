package com.hypermall.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    private Long userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer selectedCount;
    private BigDecimal subtotal;
}
