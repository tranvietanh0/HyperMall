package com.hypermall.seller.dto.request;

import com.hypermall.seller.entity.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSellerRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 150, message = "Shop name must be less than 150 characters")
    private String shopName;

    @Size(max = 500, message = "Logo URL must be less than 500 characters")
    private String logo;

    @Size(max = 500, message = "Banner URL must be less than 500 characters")
    private String banner;

    private String description;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @Size(max = 120, message = "Business license must be less than 120 characters")
    private String businessLicense;

    @Size(max = 50, message = "Tax code must be less than 50 characters")
    private String taxCode;

    @Size(max = 50, message = "Bank account number must be less than 50 characters")
    private String bankAccountNumber;

    @Size(max = 120, message = "Bank name must be less than 120 characters")
    private String bankName;

    @Size(max = 150, message = "Bank account holder must be less than 150 characters")
    private String bankAccountHolder;
}
