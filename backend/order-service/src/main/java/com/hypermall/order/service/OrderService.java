package com.hypermall.order.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.order.dto.*;
import com.hypermall.order.entity.*;
import com.hypermall.order.mapper.OrderMapper;
import com.hypermall.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDetailResponse createOrder(Long userId, CreateOrderRequest request) {
        // Calculate totals
        BigDecimal subtotal = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.valueOf(30000); // flat fee, real impl would call shipping service
        BigDecimal discount = BigDecimal.ZERO; // real impl would apply voucher
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);

        ShippingAddressRequest addrReq = request.getShippingAddress();
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .fullName(addrReq.getFullName())
                .phone(addrReq.getPhone())
                .province(addrReq.getProvince())
                .district(addrReq.getDistrict())
                .ward(addrReq.getWard())
                .addressDetail(addrReq.getAddressDetail())
                .build();

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .sellerId(request.getSellerId())
                .paymentMethod(request.getPaymentMethod())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discount(discount)
                .total(total)
                .shippingAddress(shippingAddress)
                .note(request.getNote())
                .voucherCode(request.getVoucherCode())
                .items(new ArrayList<>())
                .build();

        // Add order items
        request.getItems().forEach(itemReq -> {
            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .variantId(itemReq.getVariantId())
                    .productName(itemReq.getProductName())
                    .variantName(itemReq.getVariantName())
                    .thumbnail(itemReq.getThumbnail())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .totalPrice(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();
            order.addItem(item);
        });

        // COD orders go directly to CONFIRMED status
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        }

        Order saved = orderRepository.save(order);
        log.info("Order created: {} (ID: {}) by user {}", saved.getOrderNumber(), saved.getId(), userId);

        return orderMapper.toOrderDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = (status != null)
                ? orderRepository.findByUserIdAndStatus(userId, status, pageable)
                : orderRepository.findByUserId(userId, pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to view this order");
        }

        return orderMapper.toOrderDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return orderMapper.toOrderDetailResponse(order);
    }

    @Transactional
    public OrderDetailResponse cancelOrder(Long userId, Long orderId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to cancel this order");
        }

        List<OrderStatus> cancellableStatuses = List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAID,
                OrderStatus.CONFIRMED
        );

        if (!cancellableStatuses.contains(order.getStatus())) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request.getReason());
        order.setCancelledAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: {} (ID: {}) by user {}", saved.getOrderNumber(), saved.getId(), userId);

        return orderMapper.toOrderDetailResponse(saved);
    }

    // Seller operations
    @Transactional(readOnly = true)
    public Page<OrderResponse> getSellerOrders(Long sellerId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = (status != null)
                ? orderRepository.findBySellerIdAndStatus(sellerId, status, pageable)
                : orderRepository.findBySellerId(sellerId, pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Transactional
    public OrderDetailResponse updateOrderStatus(Long sellerId, Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't have permission to update this order");
        }

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        setStatusTimestamp(order, newStatus);

        Order saved = orderRepository.save(order);
        log.info("Order status updated: {} -> {} for order {}", order.getStatus(), newStatus, saved.getOrderNumber());

        return orderMapper.toOrderDetailResponse(saved);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING_PAYMENT -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPING;
            case SHIPPING -> next == OrderStatus.DELIVERED;
            case DELIVERED -> next == OrderStatus.COMPLETED || next == OrderStatus.RETURNED;
            default -> false;
        };

        if (!valid) {
            throw new BadRequestException(
                    "Cannot transition order status from " + current + " to " + next);
        }
    }

    private void setStatusTimestamp(Order order, OrderStatus status) {
        switch (status) {
            case PAID -> order.setPaidAt(LocalDateTime.now());
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPING -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> order.setCancelledAt(LocalDateTime.now());
            default -> { }
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        String orderNumber = "HM" + timestamp + random;

        // Ensure uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            random = ThreadLocalRandom.current().nextInt(1000, 9999);
            orderNumber = "HM" + timestamp + random;
        }
        return orderNumber;
    }
}
