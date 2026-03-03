package com.hypermall.order.mapper;

import com.hypermall.order.dto.OrderDetailResponse;
import com.hypermall.order.dto.OrderItemResponse;
import com.hypermall.order.dto.OrderResponse;
import com.hypermall.order.dto.ShippingAddressResponse;
import com.hypermall.order.entity.Order;
import com.hypermall.order.entity.OrderItem;
import com.hypermall.order.entity.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "totalItems", expression = "java(order.getItems().stream().mapToInt(com.hypermall.order.entity.OrderItem::getQuantity).sum())")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "items", source = "items")
    OrderDetailResponse toOrderDetailResponse(Order order);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    ShippingAddressResponse toShippingAddressResponse(ShippingAddress shippingAddress);
}
