package com.youthconnect.edge_functions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthconnect.edge_functions.config.OpenAIConfig;
import com.youthconnect.edge_functions.dto.ChatRequest;
import com.youthconnect.edge_functions.dto.response.ChatResponse;
import com.youthconnect.edge_functions.exception.AIServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * OpenAI Service - Production-Ready Implementation
 *
 * Handles all interactions with OpenAI APIs:
 * - Chat completions (GPT-4/GPT-3.5-turbo)
 * - Text-to-Speech (TTS) for audio learning modules
 * - Speech-to-Text (Whisper) for voice inputs
 *
 * Features:
 * - Comprehensive error handling
 * - Fallback mechanisms when AI unavailable
 * - Request/response logging
 * - Cost optimization
 *
 * @author Douglas Kings Kato
 * @version 2.0 (Production Ready)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAIConfig openAIConfig;
    private final RestTemplate openAIRestTemplate;
    private final ObjectMapper objectMapper;

    // ============================================
    // CHAT COMPLETION (GPT)
    // ============================================

    /**
     * Sends a message to OpenAI Chat API with conversation history
     *
     * Use cases:
     * - AI assistant for user questions
     * - Opportunity recommendation explanations
     * - Business advice chatbot
     *
     * @param chatRequest Contains message, system prompt, and conversation history
     * @return ChatResponse with AI response and usage metrics
     * @throws AIServiceException if OpenAI API fails
     */
    public ChatResponse chatWithAI(ChatRequest chatRequest) {
        try {
            // Check if OpenAI is configured
            if (!openAIConfig.isConfigured()) {
                log.warn("OpenAI not configured, returning fallback response");
                return createFallbackResponse();
            }

            log.debug("🤖 Sending chat request to OpenAI");

            // ============================================
            // BUILD CONVERSATION MESSAGES
            // ============================================
            List<Map<String, String>> messages = new ArrayList<>();

            // Add system prompt (defines AI behavior)
            String systemPrompt = chatRequest.getSystemPrompt() != null
                    ? chatRequest.getSystemPrompt()
                    : buildDefaultSystemPrompt();

            messages.add(Map.of("role", "system", "content", systemPrompt));

            // Add conversation history for context
            if (chatRequest.getConversationHistory() != null) {
                messages.addAll(chatRequest.getConversationHistory());
            }

            // Add current user message
            messages.add(Map.of("role", "user", "content", chatRequest.getMessage()));

            // ============================================
            // PREPARE API REQUEST
            // ============================================
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getDefaultChatModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", openAIConfig.getMaxTokens());
            requestBody.put("temperature", openAIConfig.getTemperature());

            // Prepare HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // ============================================
            // MAKE API CALL
            // ============================================
            ResponseEntity<Map> response = openAIRestTemplate.exchange(
                    openAIConfig.getChatCompletionsUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // ============================================
            // PROCESS RESPONSE
            // ============================================
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String aiResponse = (String) message.get("content");

                    log.debug("✅ Successfully received AI response ({} tokens)",
                            ((Map<?, ?>) responseBody.get("usage")).get("total_tokens"));

                    return ChatResponse.builder()
                            .response(aiResponse)
                            .usage((Map<String, Object>) responseBody.get("usage"))
                            .build();
                }
            }

            throw new AIServiceException("Failed to get valid response from OpenAI");

        } catch (HttpClientErrorException e) {
            log.error("❌ OpenAI API client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw AIServiceException.rateLimitExceeded();
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw AIServiceException.apiKeyNotConfigured();
            } else {
                throw new AIServiceException("AI service error: " + e.getStatusCode(), e);
            }

        } catch (HttpServerErrorException e) {
            log.error("❌ OpenAI API server error: {}", e.getStatusCode());
            throw new AIServiceException("AI service temporarily unavailable", e);

        } catch (Exception e) {
            log.error("❌ OpenAI API error: {}", e.getMessage(), e);
            throw new AIServiceException("AI service temporarily unavailable", e);
        }
    }

    // ============================================
    // TEXT-TO-SPEECH (TTS)
    // ============================================

    /**
     * Converts text to speech using OpenAI TTS API
     *
     * Use cases:
     * - Generate audio for learning modules
     * - Create voice messages for notifications
     * - Accessibility features
     *
     * @param text Text to convert (max 4096 characters)
     * @param voice Voice type (alloy, echo, fable, onyx, nova, shimmer)
     * @return Base64 encoded MP3 audio
     * @throws AIServiceException if conversion fails
     */
    public String textToSpeech(String text, String voice) {
        try {
            // Check if OpenAI is configured
            if (!openAIConfig.isConfigured()) {
                throw AIServiceException.apiKeyNotConfigured();
            }

            log.debug("🔊 Converting text to speech ({} characters)", text.length());

            // Validate text length (OpenAI limit)
            if (text.length() > 4096) {
                throw AIServiceException.invalidRequest("Text too long (max 4096 characters)");
            }

            // ============================================
            // PREPARE TTS REQUEST
            // ============================================
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getDefaultTtsModel());
            requestBody.put("input", text);
            requestBody.put("voice", voice != null ? voice : openAIConfig.getDefaultVoice());
            requestBody.put("response_format", "mp3");
            requestBody.put("speed", 0.9); // Slightly slower for clarity

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // ============================================
            // MAKE API CALL
            // ============================================
            ResponseEntity<byte[]> response = openAIRestTemplate.exchange(
                    openAIConfig.getTtsUrl(),
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            // ============================================
            // PROCESS RESPONSE
            // ============================================
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String base64Audio = Base64.getEncoder().encodeToString(response.getBody());
                log.debug("✅ Successfully generated speech audio ({} bytes)", response.getBody().length);
                return base64Audio;
            }

            throw new AIServiceException("Failed to generate speech");

        } catch (Exception e) {
            log.error("❌ OpenAI TTS error: {}", e.getMessage(), e);
            throw new AIServiceException("Text-to-speech service unavailable", e);
        }
    }

    // ============================================
    // SPEECH-TO-TEXT (Whisper) - ✅ FIXED
    // ============================================

    /**
     * Converts speech audio to text using OpenAI Whisper API
     *
     * ✅ FIXED: Now accepts byte[] instead of String
     *
     * Use cases:
     * - Voice questions from youth via mobile
     * - USSD audio inputs
     * - Voice search functionality
     *
     * @param audioData Audio file as byte array (max 25MB)
     * @return Transcribed text
     * @throws AIServiceException if transcription fails
     */
    public String speechToText(byte[] audioData) { // ✅ FIXED PARAMETER TYPE
        try {
            // Check if OpenAI is configured
            if (!openAIConfig.isConfigured()) {
                throw AIServiceException.apiKeyNotConfigured();
            }

            log.debug("🎤 Converting speech to text ({} bytes audio)", audioData.length);

            // Validate audio size (OpenAI limit: 25MB)
            if (audioData.length > 25 * 1024 * 1024) {
                throw AIServiceException.invalidRequest("Audio file too large (max 25 MB)");
            }

            // ============================================
            // PREPARE MULTIPART REQUEST
            // ============================================
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Create ByteArrayResource with filename
            ByteArrayResource audioResource = new ByteArrayResource(audioData) {
                @Override
                public String getFilename() {
                    return "audio.webm"; // Filename required by OpenAI
                }
            };

            body.add("file", audioResource);
            body.add("model", openAIConfig.getWhisperModel());
            body.add("language", "en"); // English as primary language

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // ============================================
            // MAKE API CALL
            // ============================================
            ResponseEntity<Map> response = openAIRestTemplate.exchange(
                    openAIConfig.getWhisperUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // ============================================
            // PROCESS RESPONSE
            // ============================================
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String transcribedText = (String) response.getBody().get("text");
                log.debug("✅ Successfully transcribed audio: '{}'", transcribedText);
                return transcribedText;
            }

            throw new AIServiceException("Failed to transcribe audio");

        } catch (Exception e) {
            log.error("❌ OpenAI Whisper error: {}", e.getMessage(), e);
            throw new AIServiceException("Speech-to-text service unavailable", e);
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Build default system prompt for AI assistant
     */
    private String buildDefaultSystemPrompt() {
        return "You are a helpful AI assistant for the Entrepreneurship Booster Platform, " +
                "designed to help Ugandan youth with business advice, opportunity guidance, " +
                "and skill development. Be concise, practical, and culturally relevant. " +
                "Focus on actionable advice for youth entrepreneurs in Uganda.";
    }

    /**
     * Create fallback response when OpenAI is unavailable
     */
    private ChatResponse createFallbackResponse() {
        return ChatResponse.builder()
                .response("I'm having trouble connecting to my AI service right now. " +
                        "However, you can still browse opportunities, apply for programs, " +
                        "access learning modules, and connect with mentors. " +
                        "Try asking your question again in a few moments.")
                .usage(Map.of(
                        "prompt_tokens", 0,
                        "completion_tokens", 0,
                        "total_tokens", 0
                ))
                .build();
    }

    // ============================================
    // SERVICE STATUS
    // ============================================

    /**
     * Check if OpenAI service is configured and available
     */
    public boolean isConfigured() {
        return openAIConfig.isConfigured();
    }

    /**
     * Get human-readable status message
     */
    public String getStatus() {
        return isConfigured()
                ? "OpenAI API configured and ready"
                : "OpenAI API not configured - AI features disabled";
    }
}