package com.youthconnect.auth_service.repository;

import com.youthconnect.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUserIdAndRevokedTrue(Long userId);
}
