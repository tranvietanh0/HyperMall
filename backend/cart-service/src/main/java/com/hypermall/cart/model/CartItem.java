package com.hypermall.cart.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private String id;
    private Long productId;
    private Long variantId;
    private Long sellerId;
    private String productName;
    private String variantName;
    private String thumbnail;
    private Integer quantity;
    private BigDecimal price;
    @Builder.Default
    private Boolean selected = true;
}
