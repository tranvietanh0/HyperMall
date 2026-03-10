package com.hypermall.inventory.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.inventory.dto.request.CreateInventoryRequest;
import com.hypermall.inventory.dto.request.ReserveStockRequest;
import com.hypermall.inventory.dto.request.UpdateStockRequest;
import com.hypermall.inventory.dto.response.InventoryResponse;
import com.hypermall.inventory.dto.response.StockCheckResponse;
import com.hypermall.inventory.dto.response.StockMovementResponse;
import com.hypermall.inventory.entity.Inventory;
import com.hypermall.inventory.entity.MovementType;
import com.hypermall.inventory.entity.StockMovement;
import com.hypermall.inventory.mapper.InventoryMapper;
import com.hypermall.inventory.repository.InventoryRepository;
import com.hypermall.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponse createInventory(Long sellerId, CreateInventoryRequest request) {
        if (inventoryRepository.existsByProductIdAndVariantId(request.getProductId(), request.getVariantId())) {
            throw new BadRequestException("Inventory already exists for this product/variant");
        }

        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .sellerId(sellerId)
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .isTrackQuantity(request.getIsTrackQuantity() != null ? request.getIsTrackQuantity() : true)
                .build();

        inventory = inventoryRepository.save(inventory);

        // Record initial stock movement
        if (request.getQuantity() > 0) {
            recordMovement(inventory, MovementType.IN, request.getQuantity(), 0, request.getQuantity(),
                    "INITIAL", null, "Initial stock", sellerId);
        }

        log.info("Inventory created for product {} variant {} with quantity {}",
                request.getProductId(), request.getVariantId(), request.getQuantity());

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId, Long variantId) {
        Inventory inventory = inventoryRepository.findByProductIdAndVariantId(productId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + " variant " + variantId));
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesByProduct(Long productId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        return inventoryMapper.toInventoryResponseList(inventories);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getSellerInventories(Long sellerId, Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findBySellerId(sellerId, pageable);
        return inventories.map(inventoryMapper::toInventoryResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockInventories(Long sellerId, Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findLowStockBySellerId(sellerId, pageable);
        return inventories.map(inventoryMapper::toInventoryResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getOutOfStockInventories(Long sellerId, Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findOutOfStockBySellerId(sellerId, pageable);
        return inventories.map(inventoryMapper::toInventoryResponse);
    }

    @Transactional
    public InventoryResponse updateStock(Long sellerId, Long inventoryId, UpdateStockRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        if (!inventory.getSellerId().equals(sellerId)) {
            throw new BadRequestException("You don't have permission to update this inventory");
        }

        int quantityBefore = inventory.getQuantity();
        int quantityChange = request.getQuantity();

        switch (request.getType()) {
            case IN:
                inventory.setQuantity(inventory.getQuantity() + quantityChange);
                break;
            case OUT:
                if (inventory.getAvailableQuantity() < quantityChange) {
                    throw new BadRequestException("Insufficient available stock");
                }
                inventory.setQuantity(inventory.getQuantity() - quantityChange);
                break;
            case ADJUSTMENT:
                inventory.setQuantity(quantityChange);
                quantityChange = quantityChange - quantityBefore;
                break;
            default:
                throw new BadRequestException("Invalid movement type for manual update");
        }

        inventory = inventoryRepository.save(inventory);

        recordMovement(inventory, request.getType(), Math.abs(quantityChange), quantityBefore,
                inventory.getQuantity(), request.getReferenceType(), request.getReferenceId(),
                request.getNote(), sellerId);

        log.info("Stock updated for inventory {}: {} {} units", inventoryId, request.getType(), quantityChange);

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Transactional
    public void reserveStock(ReserveStockRequest request) {
        for (ReserveStockRequest.ReserveItem item : request.getItems()) {
            Inventory inventory = inventoryRepository.findByProductIdAndVariantIdForUpdate(
                    item.getProductId(), item.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product " + item.getProductId()));

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product " + item.getProductId() +
                        ". Available: " + inventory.getAvailableQuantity() +
                        ", Requested: " + item.getQuantity());
            }

            int quantityBefore = inventory.getReservedQuantity();
            inventory.setReservedQuantity(inventory.getReservedQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);

            recordMovement(inventory, MovementType.RESERVE, item.getQuantity(), quantityBefore,
                    inventory.getReservedQuantity(), "ORDER", request.getOrderId(),
                    "Reserved for order " + request.getOrderId(), null);
        }

        log.info("Stock reserved for order {}", request.getOrderId());
    }

    @Transactional
    public void releaseStock(Long orderId) {
        List<StockMovement> reservations = stockMovementRepository.findByReferenceTypeAndReferenceId("ORDER", orderId);

        for (StockMovement reservation : reservations) {
            if (reservation.getType() != MovementType.RESERVE) {
                continue;
            }

            Inventory inventory = reservation.getInventory();
            int quantityBefore = inventory.getReservedQuantity();
            inventory.setReservedQuantity(
                    Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
            inventoryRepository.save(inventory);

            recordMovement(inventory, MovementType.RELEASE, reservation.getQuantity(), quantityBefore,
                    inventory.getReservedQuantity(), "ORDER", orderId,
                    "Released from order " + orderId, null);
        }

        log.info("Stock released for order {}", orderId);
    }

    @Transactional
    public void confirmStock(Long orderId) {
        List<StockMovement> reservations = stockMovementRepository.findByReferenceTypeAndReferenceId("ORDER", orderId);

        for (StockMovement reservation : reservations) {
            if (reservation.getType() != MovementType.RESERVE) {
                continue;
            }

            Inventory inventory = reservation.getInventory();
            int quantityBefore = inventory.getQuantity();

            // Reduce actual quantity and reserved quantity
            inventory.setQuantity(inventory.getQuantity() - reservation.getQuantity());
            inventory.setReservedQuantity(
                    Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
            inventoryRepository.save(inventory);

            recordMovement(inventory, MovementType.OUT, reservation.getQuantity(), quantityBefore,
                    inventory.getQuantity(), "ORDER", orderId,
                    "Sold via order " + orderId, null);
        }

        log.info("Stock confirmed for order {}", orderId);
    }

    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(List<ReserveStockRequest.ReserveItem> items) {
        List<StockCheckResponse.StockCheckItem> checkItems = new ArrayList<>();
        boolean allInStock = true;

        for (ReserveStockRequest.ReserveItem item : items) {
            Inventory inventory = inventoryRepository.findByProductIdAndVariantId(
                    item.getProductId(), item.getVariantId()).orElse(null);

            int availableQuantity = inventory != null ? inventory.getAvailableQuantity() : 0;
            boolean inStock = availableQuantity >= item.getQuantity();

            if (!inStock) {
                allInStock = false;
            }

            checkItems.add(StockCheckResponse.StockCheckItem.builder()
                    .productId(item.getProductId())
                    .variantId(item.getVariantId())
                    .requestedQuantity(item.getQuantity())
                    .availableQuantity(availableQuantity)
                    .inStock(inStock)
                    .build());
        }

        return StockCheckResponse.builder()
                .allInStock(allInStock)
                .items(checkItems)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovements(Long inventoryId, Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findByInventoryIdOrderByCreatedAtDesc(
                inventoryId, pageable);
        return movements.map(inventoryMapper::toStockMovementResponse);
    }

    private void recordMovement(Inventory inventory, MovementType type, int quantity,
                                int quantityBefore, int quantityAfter, String referenceType,
                                Long referenceId, String note, Long createdBy) {
        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .type(type)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .note(note)
                .createdBy(createdBy)
                .build();

        stockMovementRepository.save(movement);
    }
}
