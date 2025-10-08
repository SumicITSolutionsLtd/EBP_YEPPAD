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
 * Database Configuration with optimized connection pooling for Youth Connect Uganda
 *
 * This configuration class sets up HikariCP connection pool with optimized settings
 * for MySQL database. It includes environment-specific configurations and
 * performance optimizations for production workloads.
 *
 * Features:
 * - Optimized HikariCP connection pool settings
 * - MySQL-specific performance optimizations
 * - Connection leak detection
 * - Proper timeout configurations
 * - Environment-specific pool sizing
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
     * Configured with MySQL-specific optimizations and monitoring
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP DataSource for Youth Connect Uganda User Service");

        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool configuration
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setValidationTimeout(validationTimeout);

        // Pool name for monitoring and debugging
        config.setPoolName("YouthConnectUserServicePool");

        // Connection validation - crucial for production stability
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(validationTimeout);

        // Performance and reliability settings
        config.setAutoCommit(false); // Better transaction control
        config.setLeakDetectionThreshold(leakDetectionThreshold); // Detect connection leaks

        // MySQL specific optimizations for better performance
        addMySQLOptimizations(config);

        log.info("HikariCP configured with pool size: {} (min: {}, max: {})",
                minimumIdle, minimumIdle, maximumPoolSize);

        return new HikariDataSource(config);
    }

    /**
     * Adds MySQL-specific optimizations to HikariCP configuration
     * These settings improve performance and reduce memory usage
     */
    private void addMySQLOptimizations(HikariConfig config) {
        // PreparedStatement caching - improves query performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Server-side prepared statements - better for repeated queries
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Local session state - reduces network calls
        config.addDataSourceProperty("useLocalSessionState", "true");

        // Batch statement optimization - better for bulk operations
        config.addDataSourceProperty("rewriteBatchedStatements", "true");

        // Metadata caching - reduces database round trips
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");

        // Connection optimization
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // Unicode and timezone settings for Uganda context
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("serverTimezone", "UTC");

        // SSL and security settings
        config.addDataSourceProperty("useSSL", "false"); // Set to true in production
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");

        // Connection reliability
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("failOverReadOnly", "false");
        config.addDataSourceProperty("maxReconnects", "3");

        log.debug("Applied MySQL-specific optimizations to HikariCP configuration");
    }

    /**
     * Development profile specific configuration
     * Smaller pool size and more relaxed settings for local development
     */
    @Bean
    @Profile("development")
    public DataSource developmentDataSource() {
        log.info("Configuring development DataSource with relaxed settings");

        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Smaller pool for development
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(180000);
        config.setMaxLifetime(600000);

        config.setPoolName("YouthConnect-Dev-Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(false);

        // Basic MySQL settings for development
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");

        return new HikariDataSource(config);
    }

    /**
     * Test profile specific configuration
     * Minimal pool size for testing with H2 database support
     */
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        log.info("Configuring test DataSource for integration tests");

        HikariConfig config = new HikariConfig();

        // Use H2 for testing if MySQL is not available
        if (jdbcUrl.contains("h2")) {
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.h2.Driver");
        } else {
            // Use MySQL for integration tests
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }

        config.setUsername(username);
        config.setPassword(password);

        // Minimal pool for tests
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);

        config.setPoolName("YouthConnect-Test-Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }
}