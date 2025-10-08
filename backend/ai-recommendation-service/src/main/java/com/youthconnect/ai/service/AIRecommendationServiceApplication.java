package com.youthconnect.ai.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI Recommendation Service Application
 * Core AI engine for personalized recommendations
 *
 * CRITICAL FEATURES:
 * - Machine learning algorithms for opportunity matching
 * - User behavior analysis and prediction
 * - Personalized content recommendations
 * - Mentor compatibility scoring
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class AIRecommendationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AIRecommendationServiceApplication.class, args);
	}
}
