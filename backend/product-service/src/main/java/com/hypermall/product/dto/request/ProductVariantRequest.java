package com.hypermall.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    private String sku;

    @NotBlank(message = "Variant name is required")
    @Size(min = 2, max = 255, message = "Variant name must be between 2 and 255 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than 0")
    private BigDecimal salePrice;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String image;

    private Map<String, String> attributes;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be 0 or positive")
    private Integer stock;

    private Boolean isActive;
}
