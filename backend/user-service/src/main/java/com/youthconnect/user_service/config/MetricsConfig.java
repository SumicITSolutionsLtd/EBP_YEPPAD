package com.youthconnect.user_service.config;

import io.micrometer.core.instrument.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FIXED: Metrics Configuration for Entrepreneurship Booster Platform Uganda User Service
 *
 * Provides comprehensive monitoring and metrics collection including:
 * - Business metrics (registrations, logins, applications)
 * - Performance metrics (response times, database connections)
 * - System health metrics (JVM, memory, CPU)
 *
 * FIXED: Uses direct Counter/Gauge/Timer instantiation instead of builder pattern
 * to avoid builder() method resolution issues in some Micrometer versions
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final ApplicationProperties applicationProperties;
    private final DataSource dataSource;

    @Value("${spring.application.name:user-service}")
    private String applicationName;

    @Value("${app.environment:development}")
    private String environment;

    private final AtomicInteger activeDatabaseConnections = new AtomicInteger(0);

    /**
     * Configure Prometheus registry
     */
    @Bean
    @Profile("!test")
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        log.info("Configuring Prometheus metrics registry");

        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        registry.config()
                .commonTags(
                        "application", applicationName,
                        "environment", environment,
                        "service", "user-service",
                        "version", applicationProperties.getVersion()
                );

        return registry;
    }

    /**
     * User Registration Counter
     * FIXED: Using direct Counter instantiation
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry registry) {
        return registry.counter(
                "entrepreneurshipboosterplatform.user.registrations.total",
                "service", "user-service",
                "description", "Total user registrations"
        );
    }

    /**
     * User Login Counter
     * FIXED: Using direct Counter instantiation
     */
    @Bean
    public Counter userLoginCounter(MeterRegistry registry) {
        return registry.counter(
                "entrepreneurshipboosterplatform.user.logins.total",
                "service", "user-service",
                "description", "Total user logins"
        );
    }

    /**
     * Failed Login Counter
     * FIXED: Using direct Counter instantiation
     */
    @Bean
    public Counter failedLoginCounter(MeterRegistry registry) {
        return registry.counter(
                "entrepreneurshipboosterplatform.security.failed_logins.total",
                "service", "user-service",
                "type", "security",
                "description", "Total failed login attempts"
        );
    }

    /**
     * Database Connection Gauge
     * FIXED: Using direct Gauge registration
     */
    @Bean
    public Gauge databaseConnectionGauge(MeterRegistry registry) {
        return Gauge.builder("entrepreneurshipboosterplatform.database.connections.active",
                        activeDatabaseConnections, AtomicInteger::get)
                .description("Active database connections")
                .tag("service", "user-service")
                .register(registry);
    }

    /**
     * API Response Timer
     * FIXED: Using direct Timer instantiation
     */
    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return registry.timer(
                "entrepreneurshipboosterplatform.api.response.time",
                "service", "user-service",
                "description", "API response times"
        );
    }

    /**
     * Helper methods for database metrics
     */
    private double getUserCountByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ? AND is_active = TRUE";
        return executeCountQuery(sql, role);
    }

    private double getUserCountByDistrict(String district) {
        String sql = "SELECT COUNT(*) FROM youth_profiles WHERE district = ?";
        return executeCountQuery(sql, district);
    }

    private double executeCountQuery(String sql, String param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            log.warn("Failed to execute count query: {}", e.getMessage());
            return 0;
        }
    }

    public void incrementActiveDatabaseConnections() {
        activeDatabaseConnections.incrementAndGet();
    }

    public void decrementActiveDatabaseConnections() {
        activeDatabaseConnections.decrementAndGet();
    }
}