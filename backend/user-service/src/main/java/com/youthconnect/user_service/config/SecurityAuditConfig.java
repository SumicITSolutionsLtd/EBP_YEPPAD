// SecurityAuditConfig.java
package com.youthconnect.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security Audit Configuration
 *
 * Configures audit logging for security events such as login attempts,
 * authentication failures, and administrative actions.
 */
@Slf4j
@Configuration
public class SecurityAuditConfig {

    /**
     * In-memory audit event repository for development
     * In production, this should be replaced with a persistent solution
     */
    @Bean
    public AuditEventRepository auditEventRepository() {
        log.info("Configuring audit event repository");
        return new InMemoryAuditEventRepository();
    }
}