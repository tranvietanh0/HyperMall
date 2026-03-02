package com.hypermall.user.dto;

import com.hypermall.user.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must be less than 100 characters")
    private String province;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District must be less than 100 characters")
    private String district;

    @NotBlank(message = "Ward is required")
    @Size(max = 100, message = "Ward must be less than 100 characters")
    private String ward;

    @NotBlank(message = "Address detail is required")
    @Size(max = 255, message = "Address detail must be less than 255 characters")
    private String addressDetail;

    private Boolean isDefault;

    private AddressType type;
}
