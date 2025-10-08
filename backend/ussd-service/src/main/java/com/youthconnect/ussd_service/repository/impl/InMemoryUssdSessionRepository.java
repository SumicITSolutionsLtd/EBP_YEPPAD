package com.youthconnect.ussd_service.repository.impl;

import com.youthconnect.ussd_service.model.UssdSession;
import com.youthconnect.ussd_service.repository.UssdSessionRepository;
import lombok.extern.slf4j.Slf4j; // Ensure this import is present
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of UssdSessionRepository.
 * For production, consider using Redis or database-backed implementation
 * for session persistence across application restarts.
 */
@Slf4j
@Repository
public class InMemoryUssdSessionRepository implements UssdSessionRepository {

    private final Map<String, UssdSession> sessionStore = new ConcurrentHashMap<>();

    @Override
    public UssdSession save(UssdSession session) {
        if (session == null) {
            log.error("Attempted to save a null session.");
            throw new IllegalArgumentException("Session cannot be null");
        }
        if (session.getSessionId() == null || session.getSessionId().isEmpty()) {
            log.error("Attempted to save a session with a null or empty ID.");
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        sessionStore.put(session.getSessionId(), session);
        log.debug("Saved session: {} for phone: {}", session.getSessionId(), session.getPhoneNumber());
        return session;
    }

    @Override
    public Optional<UssdSession> findById(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.debug("Attempted to find session with null or empty ID. Returning Optional.empty().");
            return Optional.empty();
        }
        UssdSession session = sessionStore.get(sessionId);
        log.debug("Retrieved session: {} - Found: {}", sessionId, session != null);
        return Optional.ofNullable(session);
    }

    @Override
    public void deleteById(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.warn("Attempted to delete session with null or empty ID. No action taken.");
            return;
        }
        UssdSession removed = sessionStore.remove(sessionId);
        log.debug("Deleted session: {} - Was present: {}", sessionId, removed != null);
    }

    @Override
    public boolean existsById(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.debug("Attempted to check existence for session with null or empty ID. Returning false.");
            return false;
        }
        boolean exists = sessionStore.containsKey(sessionId);
        log.debug("Session exists check: {} - Exists: {}", sessionId, exists);
        return exists;
    }

    /**
     * Utility method to get current session count
     */
    public int getSessionCount() {
        return sessionStore.size();
    }

    /**
     * Utility method to clear all sessions (useful for testing)
     */
    public void clearAllSessions() {
        sessionStore.clear();
        log.debug("Cleared all sessions");
    }

    /**
     * Clean up expired sessions
     */
    public int cleanupExpiredSessions(int timeoutMinutes) {
        final int[] removedCount = {0}; // Use an array to allow modification in lambda
        sessionStore.entrySet().removeIf(entry -> {
            UssdSession session = entry.getValue();
            if (session.isExpired(timeoutMinutes)) {
                log.debug("Removing expired session: {}", entry.getKey());
                removedCount[0]++;
                return true;
            }
            return false;
        });

        if (removedCount[0] > 0) {
            log.info("Cleaned up {} expired sessions", removedCount[0]);
        } else {
            log.debug("No expired sessions found to clean up.");
        }

        return removedCount[0];
    }
}