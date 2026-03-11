package com.hypermall.analytics.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductItem {

    private Long productId;
    private String productName;
    private Long views;
    private Long sales;
    private BigDecimal revenue;
}
