package com.hypermall.inventory.mapper;

import com.hypermall.inventory.dto.response.InventoryResponse;
import com.hypermall.inventory.dto.response.StockMovementResponse;
import com.hypermall.inventory.entity.Inventory;
import com.hypermall.inventory.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "availableQuantity", expression = "java(inventory.getAvailableQuantity())")
    @Mapping(target = "isLowStock", expression = "java(inventory.isLowStock())")
    @Mapping(target = "isOutOfStock", expression = "java(inventory.isOutOfStock())")
    InventoryResponse toInventoryResponse(Inventory inventory);

    List<InventoryResponse> toInventoryResponseList(List<Inventory> inventories);

    @Mapping(source = "inventory.id", target = "inventoryId")
    StockMovementResponse toStockMovementResponse(StockMovement movement);

    List<StockMovementResponse> toStockMovementResponseList(List<StockMovement> movements);
}
