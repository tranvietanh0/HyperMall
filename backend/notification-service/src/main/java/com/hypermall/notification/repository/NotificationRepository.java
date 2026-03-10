package com.hypermall.notification.repository;

import com.hypermall.notification.entity.Notification;
import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationStatus;
import com.hypermall.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndChannelOrderByCreatedAtDesc(
            Long userId, NotificationChannel channel, Pageable pageable);

    Page<Notification> findByUserIdAndChannelAndTypeInOrderByCreatedAtDesc(
            Long userId, NotificationChannel channel, List<NotificationType> types, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.channel = :channel AND n.status != 'READ'")
    Long countUnreadByUserIdAndChannel(@Param("userId") Long userId, @Param("channel") NotificationChannel channel);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.channel = :channel AND n.type IN :types AND n.status != 'READ'")
    Long countUnreadByUserIdAndChannelAndTypes(
            @Param("userId") Long userId,
            @Param("channel") NotificationChannel channel,
            @Param("types") List<NotificationType> types);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.userId = :userId AND n.channel = :channel AND n.status != 'READ'")
    int markAllAsRead(@Param("userId") Long userId, @Param("channel") NotificationChannel channel, @Param("readAt") LocalDateTime readAt);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.createdAt < :before")
    List<Notification> findOldPendingNotifications(
            @Param("status") NotificationStatus status,
            @Param("before") LocalDateTime before);

    void deleteByUserIdAndCreatedAtBefore(Long userId, LocalDateTime before);
}
