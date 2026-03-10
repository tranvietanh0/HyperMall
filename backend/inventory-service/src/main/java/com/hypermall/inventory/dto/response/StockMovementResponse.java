package com.hypermall.inventory.dto.response;

import com.hypermall.inventory.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;
    private Long inventoryId;
    private MovementType type;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String referenceType;
    private Long referenceId;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
}
