package com.hypermall.inventory.repository;

import com.hypermall.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId, Pageable pageable);

    List<StockMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
