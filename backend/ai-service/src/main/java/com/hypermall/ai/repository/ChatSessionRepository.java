package com.hypermall.ai.repository;

import com.hypermall.ai.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Page<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);

    List<ChatSession> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, ChatSession.SessionStatus status);

    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' ORDER BY s.updatedAt DESC")
    List<ChatSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}
