package com.hypermall.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CheckoutPreviewResponse {
    private List<CartItemResponse> selectedItems;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;
    private BigDecimal total;
    private Map<Long, List<CartItemResponse>> itemsBySeller;
}
