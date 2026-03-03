package com.hypermall.order.dto;

import com.hypermall.order.entity.OrderStatus;
import com.hypermall.order.entity.PaymentMethod;
import com.hypermall.order.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long sellerId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;
    private BigDecimal total;
    private String note;
    private String voucherCode;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
