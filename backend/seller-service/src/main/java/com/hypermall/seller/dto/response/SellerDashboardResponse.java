package com.hypermall.seller.dto.response;

import com.hypermall.seller.entity.SellerStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerDashboardResponse {

    private Long sellerId;
    private String shopName;
    private String shopSlug;
    private SellerStatus status;
    private Double rating;
    private Integer totalProducts;
    private Integer totalFollowers;
    private LocalDateTime joinedAt;
    private LocalDateTime lastUpdatedAt;
}
