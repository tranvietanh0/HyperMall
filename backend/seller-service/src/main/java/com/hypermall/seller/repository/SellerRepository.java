package com.hypermall.seller.repository;

import com.hypermall.seller.entity.Seller;
import com.hypermall.seller.entity.SellerStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);

    Optional<Seller> findByShopSlug(String shopSlug);

    boolean existsByUserId(Long userId);

    boolean existsByShopSlug(String shopSlug);

    List<Seller> findAllByStatusOrderByCreatedAtDesc(SellerStatus status);

    @Query("""
            SELECT s FROM Seller s
            WHERE (:status IS NULL OR s.status = :status)
              AND (
                    :keyword IS NULL
                    OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.shopSlug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<Seller> searchSellers(@Param("status") SellerStatus status,
                               @Param("keyword") String keyword,
                               Pageable pageable);
}
