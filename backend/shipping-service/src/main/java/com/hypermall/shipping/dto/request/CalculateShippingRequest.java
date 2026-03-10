package com.hypermall.shipping.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateShippingRequest {

    @NotBlank(message = "From province is required")
    private String fromProvince;

    @NotBlank(message = "From district is required")
    private String fromDistrict;

    private String fromWard;

    @NotBlank(message = "To province is required")
    private String toProvince;

    @NotBlank(message = "To district is required")
    private String toDistrict;

    private String toWard;

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private Integer weight; // gram

    private Integer length; // cm
    private Integer width;  // cm
    private Integer height; // cm

    private BigDecimal codAmount;

    private BigDecimal insuranceValue;
}
