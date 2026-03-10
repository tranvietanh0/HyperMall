package com.hypermall.ai.repository;

import com.hypermall.ai.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessages(@Param("sessionId") Long sessionId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);
}
