package com.youthconnect.content_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Content Service - Main Spring Boot Application
 *
 * FEATURES:
 * - Learning Modules (multi-language audio content)
 * - Community Posts (Reddit-style forum)
 * - Comments (threaded discussions)
 * - Content Moderation
 * - Progress Tracking
 *
 * INTEGRATIONS:
 * - Eureka Service Discovery
 * - Feign Clients for inter-service communication
 * - Caffeine Caching
 * - MySQL Database
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories
public class ContentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContentServiceApplication.class, args);
	}
}