package com.hypermall.product.repository;

import com.hypermall.product.entity.Product;
import com.hypermall.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    @EntityGraph(attributePaths = {"category", "brand", "images", "variants"})
    Optional<Product> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"category", "brand", "images", "variants"})
    Optional<Product> findWithDetailsBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    Page<Product> findByBrandIdAndStatus(Long brandId, ProductStatus status, Pageable pageable);

    Page<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
            "AND (:minRating IS NULL OR p.avgRating >= :minRating)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.status = :status " +
            "AND p.category.id IN :categoryIds " +
            "ORDER BY p.totalSold DESC, p.avgRating DESC")
    List<Product> findTopSellingByCategories(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.status = :status " +
            "ORDER BY p.createdAt DESC")
    Page<Product> findNewest(@Param("status") ProductStatus status, Pageable pageable);

    long countByBrandId(Long brandId);

    long countByCategoryId(Long categoryId);
}
