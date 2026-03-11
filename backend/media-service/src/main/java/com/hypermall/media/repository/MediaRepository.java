package com.hypermall.media.repository;

import com.hypermall.media.entity.Media;
import com.hypermall.media.entity.MediaType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Media> findByUserIdAndMediaTypeOrderByCreatedAtDesc(Long userId, MediaType mediaType, Pageable pageable);

    List<Media> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    long countByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
