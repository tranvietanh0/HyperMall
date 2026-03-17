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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final int MAX_ORDER_NUMBER_RETRIES = 5;
    private static final List<OrderStatus> CANCELLABLE_STATUSES = List.of(
            OrderStatus.PENDING_PAYMENT,
            OrderStatus.PAID,
            OrderStatus.CONFIRMED
    );

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Value("${app.order.auto-cancel-after-hours:24}")
    private int autoCancelAfterHours;

    @Value("${app.order.default-shipping-fee:30000}")
    private long defaultShippingFee;

    @Transactional
    public OrderDetailResponse createOrder(Long userId, CreateOrderRequest request) {
        BigDecimal subtotal = calculateSubtotal(request);

        BigDecimal shippingFee = resolveShippingFee(request);
        BigDecimal discount = resolveDiscount(request, subtotal.add(shippingFee));
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);

        ShippingAddress shippingAddress = buildShippingAddress(request.getShippingAddress());
        Order order = buildOrder(userId, request, subtotal, shippingFee, discount, total, shippingAddress);
        addOrderItems(order, request);

        // COD orders go directly to CONFIRMED status
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setConfirmedAt(LocalDateTime.now());
        }

        // Save order with retry logic for order number generation to handle race conditions
        Order saved = saveOrderWithRetry(order);
        log.info("Order created: {} (ID: {}) by user {}", saved.getOrderNumber(), saved.getId(), userId);

        return orderMapper.toOrderDetailResponse(saved);
    }

    /**
     * Saves the order with retry logic for order number generation.
     * If a duplicate order number is detected (DataIntegrityViolationException),
     * it generates a new order number and retries.
     */
    private Order saveOrderWithRetry(Order order) {
        int attempts = 0;
        while (attempts < MAX_ORDER_NUMBER_RETRIES) {
            try {
                order.setOrderNumber(generateOrderNumber());
                return orderRepository.save(order);
            } catch (DataIntegrityViolationException e) {
                attempts++;
                log.warn("Order number collision detected, attempt {}/{}", attempts, MAX_ORDER_NUMBER_RETRIES);
                if (attempts >= MAX_ORDER_NUMBER_RETRIES) {
                    throw new BadRequestException("Failed to generate unique order number after " + MAX_ORDER_NUMBER_RETRIES + " attempts");
                }
            }
        }
        throw new BadRequestException("Failed to create order");
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
        Order order = findOrderById(orderId);
        validateUserOwnsOrder(order, userId, "view");

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
        Order order = findOrderById(orderId);
        validateUserOwnsOrder(order, userId, "cancel");

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getStatus());
        }

        markOrderCancelled(order, request.getReason());

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
        Order order = findOrderById(orderId);
        validateSellerOwnsOrder(order, sellerId, "update");

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        setStatusTimestamp(order, newStatus);

        Order saved = orderRepository.save(order);
        log.info("Order status updated: {} -> {} for order {}", order.getStatus(), newStatus, saved.getOrderNumber());

        return orderMapper.toOrderDetailResponse(saved);
    }

    private BigDecimal calculateSubtotal(CreateOrderRequest request) {
        return request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveShippingFee(CreateOrderRequest request) {
        if (request.getShippingFee() == null) {
            return BigDecimal.valueOf(defaultShippingFee);
        }

        if (request.getShippingFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Shipping fee cannot be negative");
        }

        return request.getShippingFee();
    }

    private BigDecimal resolveDiscount(CreateOrderRequest request, BigDecimal maxDiscount) {
        if (request.getDiscount() == null) {
            return BigDecimal.ZERO;
        }

        if (request.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Discount cannot be negative");
        }

        if (request.getDiscount().compareTo(maxDiscount) > 0) {
            throw new BadRequestException("Discount exceeds order total");
        }

        return request.getDiscount();
    }

    private ShippingAddress buildShippingAddress(ShippingAddressRequest addrReq) {
        return ShippingAddress.builder()
                .fullName(addrReq.getFullName())
                .phone(addrReq.getPhone())
                .province(addrReq.getProvince())
                .district(addrReq.getDistrict())
                .ward(addrReq.getWard())
                .addressDetail(addrReq.getAddressDetail())
                .build();
    }

    private Order buildOrder(
            Long userId,
            CreateOrderRequest request,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal discount,
            BigDecimal total,
            ShippingAddress shippingAddress
    ) {
        return Order.builder()
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
    }

    private void addOrderItems(Order order, CreateOrderRequest request) {
        request.getItems().forEach(itemReq -> order.addItem(toOrderItem(itemReq)));
    }

    private OrderItem toOrderItem(OrderItemRequest itemReq) {
        return OrderItem.builder()
                .productId(itemReq.getProductId())
                .variantId(itemReq.getVariantId())
                .productName(itemReq.getProductName())
                .variantName(itemReq.getVariantName())
                .thumbnail(itemReq.getThumbnail())
                .quantity(itemReq.getQuantity())
                .unitPrice(itemReq.getUnitPrice())
                .totalPrice(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                .build();
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private void validateUserOwnsOrder(Order order, Long userId, String action) {
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to " + action + " this order");
        }
    }

    private void validateSellerOwnsOrder(Order order, Long sellerId, String action) {
        if (!order.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't have permission to " + action + " this order");
        }
    }

    private void markOrderCancelled(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
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

    /**
     * Generates a unique order number using timestamp and UUID suffix.
     * Format: HM + yyyyMMddHHmmss + 8 random hex characters
     * The uniqueness is enforced by the database unique constraint with retry logic in saveOrderWithRetry().
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "HM" + timestamp + randomSuffix;
    }

    /**
     * Scheduled task that runs every hour to auto-cancel orders that have been
     * in PENDING_PAYMENT status for longer than the configured duration.
     */
    @Scheduled(fixedRateString = "${app.order.auto-cancel-check-interval-ms:3600000}")
    @Transactional
    public void autoCancelExpiredOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(autoCancelAfterHours);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PENDING_PAYMENT, cutoffTime);

        if (expiredOrders.isEmpty()) {
            log.debug("No expired orders found for auto-cancellation");
            return;
        }

        log.info("Found {} expired orders to auto-cancel", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelReason("Auto-cancelled due to payment timeout after " + autoCancelAfterHours + " hours");
                order.setCancelledAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Auto-cancelled order: {} (ID: {})", order.getOrderNumber(), order.getId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel order: {} (ID: {})", order.getOrderNumber(), order.getId(), e);
            }
        }
    }
}
