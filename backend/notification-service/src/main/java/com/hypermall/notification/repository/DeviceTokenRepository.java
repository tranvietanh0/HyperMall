package com.hypermall.notification.repository;

import com.hypermall.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(Long userId);

    Optional<DeviceToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.token = :token")
    int deactivateToken(@Param("token") String token);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.isActive = false WHERE d.userId = :userId")
    int deactivateAllUserTokens(@Param("userId") Long userId);

    void deleteByUserIdAndToken(Long userId, String token);
}
