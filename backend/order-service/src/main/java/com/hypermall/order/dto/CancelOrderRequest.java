package com.hypermall.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @NotBlank(message = "Cancel reason is required")
    private String reason;
}
