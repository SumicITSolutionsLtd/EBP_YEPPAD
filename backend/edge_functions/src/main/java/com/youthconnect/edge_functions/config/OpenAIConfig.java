package com.youthconnect.edge_functions.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;  // ✅ FIXED: Use jakarta instead of javax
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI API Configuration
 *
 * Manages OpenAI API integration for:
 * - Chat completions (GPT-4/GPT-3.5)
 * - Text-to-Speech (TTS) for audio learning
 * - Speech-to-Text (Whisper) for voice inputs
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "openai.api")
@Getter
@Setter
@Slf4j
public class OpenAIConfig {

    private String key;
    private String baseUrl = "https://api.openai.com/v1";
    private String defaultChatModel = "gpt-4o-mini";
    private String defaultTtsModel = "tts-1";
    private String defaultVoice = "alloy";
    private String whisperModel = "whisper-1";
    private int requestTimeoutMs = 30000;
    private int maxTokens = 200;
    private double temperature = 0.7;
    private boolean enabled = true;

    /**
     * Validate configuration on startup
     */
    @PostConstruct  // ✅ FIXED
    public void validateConfig() {
        if (enabled) {
            if (key == null || key.isEmpty() || "your-openai-key-here".equals(key)) {
                log.warn("⚠️ OpenAI API key not configured!");
                enabled = false;
            } else {
                log.info("✅ OpenAI API configured successfully");
            }
        }
    }

    /**
     * RestTemplate for OpenAI API calls
     */
    @Bean(name = "openAIRestTemplate")
    public RestTemplate openAIRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

        // Authorization interceptor
        interceptors.add((request, body, execution) -> {
            if (key != null && !key.isEmpty()) {
                request.getHeaders().set("Authorization", "Bearer " + key);
            }
            request.getHeaders().set("Content-Type", "application/json");
            return execution.execute(request, body);
        });

        // Logging interceptor
        interceptors.add((request, body, execution) -> {
            log.debug("OpenAI API Request: {} {}", request.getMethod(), request.getURI());
            long startTime = System.currentTimeMillis();
            var response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("OpenAI API Response: {} - Duration: {}ms", response.getStatusCode(), duration);
            return response;
        });

        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    public String getChatCompletionsUrl() {
        return baseUrl + "/chat/completions";
    }

    public String getTtsUrl() {
        return baseUrl + "/audio/speech";
    }

    public String getWhisperUrl() {
        return baseUrl + "/audio/transcriptions";
    }

    public boolean isConfigured() {
        return enabled && key != null && !key.isEmpty() && !"your-openai-key-here".equals(key);
    }
}