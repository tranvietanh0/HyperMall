package com.hypermall.user.dto;

import com.hypermall.user.entity.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private Boolean isDefault;
    private AddressType type;

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", addressDetail, ward, district, province);
    }
}
