package com.hypermall.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase with hyphens")
    @Size(min = 2, max = 150, message = "Slug must be between 2 and 150 characters")
    private String slug;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String image;

    private Long parentId;

    @Min(value = 0, message = "Level must be 0 or positive")
    private Integer level;

    @Min(value = 0, message = "Sort order must be 0 or positive")
    private Integer sortOrder;

    private Boolean isActive;
}
