package com.youthconnect.notification.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * REDIS CONFIGURATION - DISTRIBUTED CACHING
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Configures Redis for distributed caching and session management.
 *
 * Use Cases:
 * - Rate limiting counters (requests per minute tracking)
 * - Notification delivery status caching
 * - User preference caching
 * - Duplicate notification prevention (deduplication)
 * - SMS/Email template caching
 *
 * Configuration Strategy:
 * - Lettuce client for high-performance async operations
 * - JSON serialization for complex objects
 * - String serialization for keys
 * - Connection pooling for concurrent requests
 *
 * Cache Regions:
 * - user-preferences: TTL 1 hour
 * - rate-limits: TTL 1 minute
 * - templates: TTL 30 minutes
 * - delivery-status: TTL 24 hours
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * Redis connection factory using Lettuce client.
     *
     * Lettuce vs Jedis:
     * - Lettuce supports async/reactive operations
     * - Better performance under high concurrency
     * - Built-in connection pooling
     * - Thread-safe by default
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("🔧 Configuring Redis connection: host={}, port={}, database={}",
                redisHost, redisPort, redisDatabase);

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);

        // Set password only if provided
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
            log.info("✅ Redis password authentication enabled");
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        log.info("✅ Redis connection factory initialized successfully");
        return factory;
    }

    /**
     * RedisTemplate for generic operations with JSON serialization.
     *
     * Serialization Strategy:
     * - Keys: String serialization (human-readable)
     * - Values: JSON serialization (preserves object structure)
     * - Hash Keys: String serialization
     * - Hash Values: JSON serialization
     *
     * Benefits:
     * - Complex objects stored as JSON
     * - Easy debugging with Redis CLI
     * - Type-safe deserialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        log.info("🔧 Configuring RedisTemplate with JSON serialization");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Configure ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Support for Java 8 date/time
        objectMapper.findAndRegisterModules();

        // Key serialization: String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Value serialization: JSON
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Apply serializers
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        log.info("✅ RedisTemplate configured successfully");
        return template;
    }

    /**
     * Specialized RedisTemplate for rate limiting.
     * Uses simple String serialization for counters.
     */
    @Bean("rateLimitRedisTemplate")
    public RedisTemplate<String, Integer> rateLimitRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);

        template.afterPropertiesSet();

        log.info("✅ Rate limit RedisTemplate configured");
        return template;
    }
}