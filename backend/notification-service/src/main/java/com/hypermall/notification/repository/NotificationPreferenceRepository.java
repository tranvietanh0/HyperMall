package com.hypermall.notification.repository;

import com.hypermall.notification.entity.NotificationChannel;
import com.hypermall.notification.entity.NotificationPreference;
import com.hypermall.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByUserId(Long userId);

    Optional<NotificationPreference> findByUserIdAndTypeAndChannel(Long userId, NotificationType type, NotificationChannel channel);

    List<NotificationPreference> findByUserIdAndEnabledTrue(Long userId);

    boolean existsByUserIdAndTypeAndChannelAndEnabledTrue(Long userId, NotificationType type, NotificationChannel channel);
}
