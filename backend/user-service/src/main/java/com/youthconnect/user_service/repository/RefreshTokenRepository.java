package com.youthconnect.user_service.repository;

import com.youthconnect.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token Repository
 * Updated to use UUID for user reference
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUserIdAndRevokedTrue(UUID userId);
}