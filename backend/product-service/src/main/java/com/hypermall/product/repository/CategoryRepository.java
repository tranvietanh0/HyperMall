package com.hypermall.product.repository;

import com.hypermall.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(Long parentId);

    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findRootCategoriesActive();

    long countByParentId(Long parentId);
}
