package com.youthconnect.notification.service;

import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION LOG REPOSITORY TESTS (FIXED - UUID SUPPORT)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@DataJpaTest
class NotificationLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationLogRepository repository;

    @Test
    void testSaveNotificationLog() {
        // ✅ FIXED: Use UUID
        UUID testUserId = UUID.randomUUID();

        NotificationLog log = NotificationLog.builder()
                .userId(testUserId)
                .notificationType(NotificationLog.NotificationType.SMS)
                .recipient("+256701234567")
                .content("Test message")
                .status(NotificationLog.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .provider("AFRICAS_TALKING")
                .build();

        NotificationLog saved = repository.save(log);

        assertNotNull(saved.getId());
        assertEquals("+256701234567", saved.getRecipient());
        assertEquals(NotificationLog.NotificationStatus.PENDING, saved.getStatus());
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        UUID testUserId = UUID.randomUUID();

        NotificationLog log1 = createTestLog(testUserId, "First message");
        NotificationLog log2 = createTestLog(testUserId, "Second message");

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.flush();

        // ✅ FIXED: Use Pageable
        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationLog> logs = repository.findByUserIdOrderByCreatedAtDesc(testUserId, pageable);

        assertEquals(2, logs.getTotalElements());
        assertTrue(logs.getContent().get(0).getCreatedAt().isAfter(logs.getContent().get(1).getCreatedAt()) ||
                logs.getContent().get(0).getCreatedAt().isEqual(logs.getContent().get(1).getCreatedAt()));
    }

    @Test
    void testFindPendingRetries() {
        UUID testUserId = UUID.randomUUID();

        NotificationLog failedLog = NotificationLog.builder()
                .userId(testUserId)
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

        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationLog> pendingRetries = repository.findPendingRetries(LocalDateTime.now(), pageable);

        assertEquals(1, pendingRetries.getTotalElements());
        assertEquals(NotificationLog.NotificationStatus.FAILED, pendingRetries.getContent().get(0).getStatus());
    }

    @Test
    void testCountByStatusAndCreatedAtAfter() {
        UUID testUserId1 = UUID.randomUUID();
        UUID testUserId2 = UUID.randomUUID();

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        NotificationLog log1 = createTestLog(testUserId1, "Message 1");
        log1.setStatus(NotificationLog.NotificationStatus.SENT);
        log1.setCreatedAt(LocalDateTime.now());

        NotificationLog log2 = createTestLog(testUserId2, "Message 2");
        log2.setStatus(NotificationLog.NotificationStatus.SENT);
        log2.setCreatedAt(LocalDateTime.now());

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.flush();

        long count = repository.countByStatusAndCreatedAtAfter(
                NotificationLog.NotificationStatus.SENT,
                oneDayAgo
        );

        assertEquals(2, count);
    }

    private NotificationLog createTestLog(UUID userId, String content) {
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