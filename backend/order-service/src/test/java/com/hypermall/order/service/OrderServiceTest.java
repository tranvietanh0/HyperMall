package com.hypermall.order.service;

import com.hypermall.order.dto.CreateOrderRequest;
import com.hypermall.order.dto.OrderDetailResponse;
import com.hypermall.order.dto.OrderItemRequest;
import com.hypermall.order.dto.ShippingAddressRequest;
import com.hypermall.order.entity.Order;
import com.hypermall.order.entity.OrderStatus;
import com.hypermall.order.entity.PaymentMethod;
import com.hypermall.order.mapper.OrderMapper;
import com.hypermall.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "defaultShippingFee", 30000L);
    }

    @Test
    @DisplayName("Should create a COD order with calculated totals and confirmed status")
    void createOrder_WithCodPayment_ShouldPersistCalculatedTotals() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSellerId(99L);
        request.setPaymentMethod(PaymentMethod.COD);
        request.setShippingAddress(createShippingAddress());

        OrderItemRequest firstItem = new OrderItemRequest();
        firstItem.setProductId(12L);
        firstItem.setVariantId(102L);
        firstItem.setProductName("Product 2");
        firstItem.setVariantName("Variant 2");
        firstItem.setThumbnail("https://example.com/item-2.png");
        firstItem.setQuantity(2);
        firstItem.setUnitPrice(new BigDecimal("150000"));

        OrderItemRequest secondItem = new OrderItemRequest();
        secondItem.setProductId(11L);
        secondItem.setVariantId(101L);
        secondItem.setProductName("Product 1");
        secondItem.setVariantName("Variant 1");
        secondItem.setThumbnail("https://example.com/item-1.png");
        secondItem.setQuantity(1);
        secondItem.setUnitPrice(new BigDecimal("50000"));

        request.setItems(List.of(firstItem, secondItem));
        request.setNote("leave at door");
        request.setVoucherCode("SALE10");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toOrderDetailResponse(any(Order.class))).thenReturn(new OrderDetailResponse());

        orderService.createOrder(7L, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUserId()).isEqualTo(7L);
        assertThat(savedOrder.getSellerId()).isEqualTo(99L);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(savedOrder.getConfirmedAt()).isNotNull();
        assertThat(savedOrder.getSubtotal()).isEqualByComparingTo(new BigDecimal("350000"));
        assertThat(savedOrder.getShippingFee()).isEqualByComparingTo(new BigDecimal("30000"));
        assertThat(savedOrder.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedOrder.getTotal()).isEqualByComparingTo(new BigDecimal("380000"));
        assertThat(savedOrder.getItems()).hasSize(2);
        assertThat(savedOrder.getItems()).allMatch(item -> item.getOrder() == savedOrder);
        assertThat(savedOrder.getShippingAddress().getFullName()).isEqualTo("Buyer One");
    }

    private ShippingAddressRequest createShippingAddress() {
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setFullName("Buyer One");
        request.setPhone("0987654321");
        request.setProvince("HCM");
        request.setDistrict("District 1");
        request.setWard("Ben Nghe");
        request.setAddressDetail("1 Nguyen Hue");
        return request;
    }

}
