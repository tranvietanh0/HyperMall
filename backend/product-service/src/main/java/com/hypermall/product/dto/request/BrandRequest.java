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
public class BrandRequest {

    @NotBlank(message = "Brand name is required")
    @Size(min = 2, max = 100, message = "Brand name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase with hyphens")
    @Size(min = 2, max = 150, message = "Slug must be between 2 and 150 characters")
    private String slug;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logo;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
