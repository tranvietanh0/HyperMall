package com.hypermall.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private Boolean isActive;
    private List<CategoryResponse> children;
    private LocalDateTime createdAt;
}
