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
public class TopCategoryItem {

    private Long categoryId;
    private String categoryName;
    private Long views;
    private Long sales;
    private BigDecimal revenue;
}
