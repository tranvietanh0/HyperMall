package com.hypermall.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String image;
    private Map<String, String> attributes;
    private Integer stock;
    private Boolean isActive;
}
