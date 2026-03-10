package com.hypermall.notification.repository;

import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationTemplate;
import com.hypermall.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTypeAndChannelAndIsActiveTrue(NotificationType type, NotificationChannel channel);

    List<NotificationTemplate> findByTypeAndIsActiveTrue(NotificationType type);

    List<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);

    boolean existsByTypeAndChannel(NotificationType type, NotificationChannel channel);
}
