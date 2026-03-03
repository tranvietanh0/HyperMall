package com.hypermall.payment.mapper;

import com.hypermall.payment.dto.PaymentResponse;
import com.hypermall.payment.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toPaymentResponse(Payment payment);
}
