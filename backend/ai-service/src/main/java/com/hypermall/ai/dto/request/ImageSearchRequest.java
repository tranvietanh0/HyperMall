package com.hypermall.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchRequest {

    private String imageUrl;
    private String imageBase64;
    private Long categoryId;
    private int limit;
}
