package com.youthconnect.notification.service;

import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for NotificationLogRepository.
 */
@DataJpaTest
class NotificationLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationLogRepository repository;

    @Test
    void testSaveNotificationLog() {
        // Arrange
        NotificationLog log = NotificationLog.builder()
                .userId(1L)
                .notificationType(NotificationLog.NotificationType.SMS)
                .recipient("+256701234567")
                .content("Test message")
                .status(NotificationLog.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .provider("AFRICAS_TALKING")
                .build();

        // Act
        NotificationLog saved = repository.save(log);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("+256701234567", saved.getRecipient());
        assertEquals(NotificationLog.NotificationStatus.PENDING, saved.getStatus());
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        // Arrange
        NotificationLog log1 = createTestLog(1L, "First message");
        NotificationLog log2 = createTestLog(1L, "Second message");

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.flush();

        // Act
        List<NotificationLog> logs = repository.findByUserIdOrderByCreatedAtDesc(1L);

        // Assert
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).getCreatedAt().isAfter(logs.get(1).getCreatedAt()) ||
                logs.get(0).getCreatedAt().isEqual(logs.get(1).getCreatedAt()));
    }

    @Test
    void testFindPendingRetries() {
        // Arrange
        NotificationLog failedLog = NotificationLog.builder()
                .userId(1L)
                .notificationType(NotificationLog.NotificationType.SMS)
                .recipient("+256701234567")
                .content("Test message")
                .status(NotificationLog.NotificationStatus.FAILED)
                .retryCount(1)
                .maxRetries(3)
                .nextRetryAt(LocalDateTime.now().minusMinutes(5))
                .build();

        entityManager.persist(failedLog);
        entityManager.flush();

        // Act
        List<NotificationLog> pendingRetries = repository.findPendingRetries(LocalDateTime.now());

        // Assert
        assertEquals(1, pendingRetries.size());
        assertEquals(NotificationLog.NotificationStatus.FAILED, pendingRetries.get(0).getStatus());
    }

    @Test
    void testCountByStatusAndCreatedAtAfter() {
        // Arrange
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        NotificationLog log1 = createTestLog(1L, "Message 1");
        log1.setStatus(NotificationLog.NotificationStatus.SENT);
        log1.setCreatedAt(LocalDateTime.now());

        NotificationLog log2 = createTestLog(2L, "Message 2");
        log2.setStatus(NotificationLog.NotificationStatus.SENT);
        log2.setCreatedAt(LocalDateTime.now());

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.flush();

        // Act
        long count = repository.countByStatusAndCreatedAtAfter(
                NotificationLog.NotificationStatus.SENT,
                oneDayAgo
        );

        // Assert
        assertEquals(2, count);
    }

    private NotificationLog createTestLog(Long userId, String content) {
        return NotificationLog.builder()
                .userId(userId)
                .notificationType(NotificationLog.NotificationType.SMS)
                .recipient("+256701234567")
                .content(content)
                .status(NotificationLog.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .provider("TEST")
                .createdAt(LocalDateTime.now())
                .build();
    }
}