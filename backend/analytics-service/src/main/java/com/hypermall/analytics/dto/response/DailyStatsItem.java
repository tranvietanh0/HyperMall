package com.hypermall.analytics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsItem {

    private LocalDate date;
    private Long orders;
    private BigDecimal revenue;
    private Long pageViews;
    private Long users;
}
