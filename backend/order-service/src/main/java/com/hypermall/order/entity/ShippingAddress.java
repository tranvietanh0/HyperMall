package com.hypermall.order.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
}
