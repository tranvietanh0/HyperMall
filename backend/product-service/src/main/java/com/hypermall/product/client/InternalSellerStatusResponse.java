package com.hypermall.product.client;

public record InternalSellerStatusResponse(
        Long sellerId,
        Long userId,
        String shopSlug,
        String status,
        boolean active
) {
}
