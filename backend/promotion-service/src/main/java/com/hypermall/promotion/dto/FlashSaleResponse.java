package com.hypermall.promotion.dto;

import com.hypermall.promotion.entity.FlashSaleStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlashSaleResponse {
    private Long id;
    private String name;
    private String description;
    private String bannerImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private FlashSaleStatus status;
    private boolean active;
    private boolean upcoming;
    private long remainingSeconds;
    private List<FlashSaleProductResponse> products;
    private LocalDateTime createdAt;
}
