package com.hypermall.seller.dto.response;

import com.hypermall.seller.entity.BusinessType;
import com.hypermall.seller.entity.SellerStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerResponse {
    private Long id;
    private Long userId;
    private String shopName;
    private String shopSlug;
    private String logo;
    private String banner;
    private String description;
    private BusinessType businessType;
    private String businessLicense;
    private String taxCode;
    private String bankAccountNumber;
    private String bankName;
    private String bankAccountHolder;
    private SellerStatus status;
    private Double rating;
    private Integer totalProducts;
    private Integer totalFollowers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
