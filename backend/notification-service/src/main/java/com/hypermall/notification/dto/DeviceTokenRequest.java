package com.hypermall.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    private String token;

    private String deviceType;

    private String deviceName;
}
