package com.hypermall.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ZaloPayCallbackRequest {
    private Map<String, Object> data;
    private String mac;
    private Integer type;
}
