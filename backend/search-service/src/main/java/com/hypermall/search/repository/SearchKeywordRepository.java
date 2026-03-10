package com.hypermall.search.repository;

import com.hypermall.search.document.SearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends ElasticsearchRepository<SearchKeyword, String> {

    Optional<SearchKeyword> findByKeyword(String keyword);

    List<SearchKeyword> findByKeywordContainingIgnoreCaseOrderBySearchCountDesc(String keyword, Pageable pageable);

    List<SearchKeyword> findAllByOrderBySearchCountDesc(Pageable pageable);
}
