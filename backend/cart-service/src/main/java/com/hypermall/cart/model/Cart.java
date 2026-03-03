package com.hypermall.cart.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart implements Serializable {

    private Long userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .filter(CartItem::getSelected)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getSelectedCount() {
        return (int) items.stream().filter(CartItem::getSelected).count();
    }
}
