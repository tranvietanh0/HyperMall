package com.hypermall.inventory.repository;

import com.hypermall.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndVariantId(Long productId, Long variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.variantId = :variantId")
    Optional<Inventory> findByProductIdAndVariantIdForUpdate(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId);

    List<Inventory> findByProductId(Long productId);

    Page<Inventory> findBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.sellerId = :sellerId AND (i.quantity - i.reservedQuantity) <= i.lowStockThreshold")
    Page<Inventory> findLowStockBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.sellerId = :sellerId AND (i.quantity - i.reservedQuantity) <= 0")
    Page<Inventory> findOutOfStockBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds")
    List<Inventory> findByProductIdIn(@Param("productIds") List<Long> productIds);

    boolean existsByProductIdAndVariantId(Long productId, Long variantId);
}
