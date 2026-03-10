package com.hypermall.seller.repository;

import com.hypermall.seller.entity.Seller;
import com.hypermall.seller.entity.SellerStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);

    Optional<Seller> findByShopSlug(String shopSlug);

    boolean existsByUserId(Long userId);

    boolean existsByShopSlug(String shopSlug);

    List<Seller> findAllByStatusOrderByCreatedAtDesc(SellerStatus status);
}
