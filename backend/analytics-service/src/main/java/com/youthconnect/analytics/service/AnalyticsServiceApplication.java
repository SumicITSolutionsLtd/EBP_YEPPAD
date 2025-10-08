package com.youthconnect.analytics.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Analytics Service Application
 * Comprehensive analytics and reporting for all stakeholders
 *
 * CRITICAL FEATURES:
 * - Real-time dashboard analytics
 * - NGO performance monitoring
 * - Funder impact tracking
 * - Report generation (PDF, CSV, Excel)
 * - Platform-wide metrics
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableScheduling // For scheduled analytics tasks
public class AnalyticsServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AnalyticsServiceApplication.class, args);
	}
}
