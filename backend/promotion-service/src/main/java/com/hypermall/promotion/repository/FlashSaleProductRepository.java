package com.hypermall.promotion.repository;

import com.hypermall.promotion.entity.FlashSaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Long> {

    List<FlashSaleProduct> findByFlashSaleIdOrderBySortOrderAsc(Long flashSaleId);

    Optional<FlashSaleProduct> findByFlashSaleIdAndProductId(Long flashSaleId, Long productId);

    @Modifying
    @Query("UPDATE FlashSaleProduct fsp SET fsp.soldCount = fsp.soldCount + :quantity " +
            "WHERE fsp.id = :id AND fsp.soldCount + :quantity <= fsp.stockLimit")
    int incrementSoldCount(@Param("id") Long id, @Param("quantity") int quantity);

    @Query("SELECT fsp FROM FlashSaleProduct fsp JOIN fsp.flashSale fs " +
            "WHERE fsp.productId = :productId AND fs.status = 'ACTIVE' " +
            "AND fs.startTime <= CURRENT_TIMESTAMP AND fs.endTime >= CURRENT_TIMESTAMP")
    Optional<FlashSaleProduct> findActiveFlashSaleProduct(@Param("productId") Long productId);
}
