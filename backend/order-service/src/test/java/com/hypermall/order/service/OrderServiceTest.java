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
        request.setItems(List.of(createOrderItem(2, "150000"), createOrderItem(1, "50000")));
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

    private OrderItemRequest createOrderItem(int quantity, String unitPrice) {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(10L + quantity);
        request.setVariantId(100L + quantity);
        request.setProductName("Product " + quantity);
        request.setVariantName("Variant " + quantity);
        request.setThumbnail("https://example.com/item-" + quantity + ".png");
        request.setQuantity(quantity);
        request.setUnitPrice(new BigDecimal(unitPrice));
        return request;
    }
}
