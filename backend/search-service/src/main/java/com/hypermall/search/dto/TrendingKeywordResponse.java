package com.hypermall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingKeywordResponse {

    private String keyword;
    private Long searchCount;
    private Long rank;
    private boolean trending;
}
