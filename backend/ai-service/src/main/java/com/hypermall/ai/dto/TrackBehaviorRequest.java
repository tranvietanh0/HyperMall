package com.hypermall.ai.dto;

import com.hypermall.ai.entity.UserBehavior;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackBehaviorRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Behavior type is required")
    private UserBehavior.BehaviorType behaviorType;

    private String searchQuery;
    private Long categoryId;
    private Long brandId;
}
