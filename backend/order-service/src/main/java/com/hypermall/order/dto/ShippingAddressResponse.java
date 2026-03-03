package com.hypermall.order.dto;

import lombok.Data;

@Data
public class ShippingAddressResponse {
    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
}
