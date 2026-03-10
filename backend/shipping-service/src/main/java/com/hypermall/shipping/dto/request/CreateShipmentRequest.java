package com.hypermall.shipping.dto.request;

import com.hypermall.shipping.entity.ShippingProvider;
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
public class CreateShipmentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Provider is required")
    private ShippingProvider provider;

    // Sender
    @NotBlank(message = "Sender name is required")
    private String senderName;

    @NotBlank(message = "Sender phone is required")
    private String senderPhone;

    @NotBlank(message = "Sender address is required")
    private String senderAddress;

    @NotBlank(message = "Sender province is required")
    private String senderProvince;

    @NotBlank(message = "Sender district is required")
    private String senderDistrict;

    private String senderWard;

    // Receiver
    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    @NotBlank(message = "Receiver address is required")
    private String receiverAddress;

    @NotBlank(message = "Receiver province is required")
    private String receiverProvince;

    @NotBlank(message = "Receiver district is required")
    private String receiverDistrict;

    private String receiverWard;

    // Package
    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private Integer weight;

    private Integer length;
    private Integer width;
    private Integer height;

    private BigDecimal codAmount;
    private BigDecimal insuranceValue;

    private String note;
}
