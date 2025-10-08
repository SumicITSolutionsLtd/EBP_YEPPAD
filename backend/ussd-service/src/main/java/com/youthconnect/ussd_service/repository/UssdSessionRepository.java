package com.youthconnect.ussd_service.repository;

import com.youthconnect.ussd_service.model.UssdSession;
import java.util.Optional;

/**
 * Repository interface for USSD session management
 */
public interface UssdSessionRepository {
    UssdSession save(UssdSession session);
    Optional<UssdSession> findById(String sessionId);
    void deleteById(String sessionId);
    boolean existsById(String sessionId);
}