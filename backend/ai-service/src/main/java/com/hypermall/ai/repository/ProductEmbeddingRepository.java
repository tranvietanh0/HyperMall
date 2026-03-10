package com.hypermall.ai.repository;

import com.hypermall.ai.entity.ProductEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {

    Optional<ProductEmbedding> findByProductId(Long productId);

    List<ProductEmbedding> findByProductIdIn(List<Long> productIds);

    boolean existsByProductId(Long productId);

    void deleteByProductId(Long productId);
}
