package com.youthconnect.user_service.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DATABASE CONFIGURATION - POSTGRESQL (FIXED)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * FIXES APPLIED:
 * 1. Changed from MySQL to PostgreSQL driver
 * 2. Updated connection pool settings for PostgreSQL
 * 3. Removed MySQL-specific optimizations
 * 4. Added PostgreSQL-specific configurations
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (PostgreSQL Migration)
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.validation-timeout:5000}")
    private long validationTimeout;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    /**
     * Primary DataSource bean with HikariCP connection pool
     * Configured for PostgreSQL with performance optimizations
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP DataSource for PostgreSQL");

        HikariConfig config = new HikariConfig();

        // Basic connection settings - FIXED: PostgreSQL driver
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver"); // FIXED: Changed from MySQL

        // Pool configuration
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setValidationTimeout(validationTimeout);

        // Pool name for monitoring
        config.setPoolName("YouthConnectUserServicePool");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1"); // Works for both MySQL and PostgreSQL
        config.setValidationTimeout(validationTimeout);

        // Performance and reliability settings
        config.setAutoCommit(true); // FIXED: Changed to true for PostgreSQL
        config.setLeakDetectionThreshold(leakDetectionThreshold);

        // PostgreSQL-specific optimizations
        addPostgreSQLOptimizations(config);

        log.info("HikariCP configured with pool size: {} (min: {}, max: {})",
                minimumIdle, minimumIdle, maximumPoolSize);

        return new HikariDataSource(config);
    }

    /**
     * FIXED: PostgreSQL-specific optimizations
     * Replaces MySQL optimizations with PostgreSQL equivalents
     */
    private void addPostgreSQLOptimizations(HikariConfig config) {
        // PreparedStatement caching - PostgreSQL supports this
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepareThreshold", "3");

        // Connection optimization
        config.addDataSourceProperty("defaultRowFetchSize", "50");

        // SSL settings (disable for local dev, enable in production)
        config.addDataSourceProperty("ssl", "false");
        config.addDataSourceProperty("sslmode", "prefer");

        // TCP keep-alive settings
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("socketTimeout", "30");

        // Application name for monitoring
        config.addDataSourceProperty("ApplicationName", "YouthConnectUserService");

        // Log unclosed connections
        config.addDataSourceProperty("logUnclosedConnections", "true");

        log.debug("Applied PostgreSQL-specific optimizations to HikariCP configuration");
    }

    /**
     * Development profile specific configuration with smaller pool
     */
    @Bean
    @Profile("dev")
    public DataSource developmentDataSource() {
        log.info("Configuring development DataSource with relaxed settings");

        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Smaller pool for development
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(180000);
        config.setMaxLifetime(600000);

        config.setPoolName("YouthConnect-Dev-Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);

        // Basic PostgreSQL settings for development
        config.addDataSourceProperty("ssl", "false");
        config.addDataSourceProperty("ApplicationName", "YouthConnectUserService-Dev");

        return new HikariDataSource(config);
    }
}