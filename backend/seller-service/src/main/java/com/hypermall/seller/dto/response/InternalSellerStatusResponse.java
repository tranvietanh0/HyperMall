package com.hypermall.seller.dto.response;

import com.hypermall.seller.entity.SellerStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InternalSellerStatusResponse {
    private Long sellerId;
    private Long userId;
    private String shopSlug;
    private SellerStatus status;
    private boolean active;
}
