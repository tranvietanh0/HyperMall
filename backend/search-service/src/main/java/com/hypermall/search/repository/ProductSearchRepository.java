package com.hypermall.search.repository;

import com.hypermall.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ProductDocument> findByCategoryId(Long categoryId, Pageable pageable);

    Page<ProductDocument> findByBrandId(Long brandId, Pageable pageable);

    Page<ProductDocument> findBySellerId(Long sellerId, Pageable pageable);

    Page<ProductDocument> findByStatus(String status, Pageable pageable);

    List<ProductDocument> findByNameContainingIgnoreCaseOrderByTotalSoldDesc(String name, Pageable pageable);

    void deleteByIdIn(List<String> ids);
}
