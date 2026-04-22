package com.hypermall.product.service;

import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.product.client.InternalSellerStatusResponse;
import com.hypermall.product.client.SellerStatusClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerGuardService {

    private final SellerStatusClient sellerStatusClient;

    public void requireActiveSeller(Long sellerUserId) {
        InternalSellerStatusResponse seller = sellerStatusClient.getSellerStatusByUserId(sellerUserId);

        if (seller.active()) {
            return;
        }

        if ("PENDING".equals(seller.status())) {
            throw new ForbiddenException("Seller account is pending approval");
        }

        if ("SUSPENDED".equals(seller.status())) {
            throw new ForbiddenException("Seller account is suspended");
        }

        throw new ForbiddenException("Seller account is not active");
    }
}
