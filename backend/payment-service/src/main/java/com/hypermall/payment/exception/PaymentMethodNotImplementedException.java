package com.hypermall.payment.exception;

import com.hypermall.common.exception.BadRequestException;

public class PaymentMethodNotImplementedException extends BadRequestException {

    public PaymentMethodNotImplementedException(String paymentMethod) {
        super("Payment method '" + paymentMethod + "' is not yet implemented. " +
                "Please use one of the supported methods: COD, VNPAY, MOMO, ZALOPAY.");
    }
}
