package com.youthconnect.edge_functions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthconnect.edge_functions.dto.ChatRequest;
import com.youthconnect.edge_functions.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Base64;

/**
 * OpenAIService handles all interactions with OpenAI APIs including:
 * - Chat completions (GPT models)
 * - Text-to-speech (TTS)
 * - Speech-to-text (Whisper)
 *
 * This service uses environment variables for configuration to ensure
 * security and flexibility across different deployment environments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final Environment environment;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Retrieves the OpenAI API key from environment variables with fallback logic
     * @return OpenAI API key or default placeholder if not configured
     */
    private String getOpenAIApiKey() {
        // Try multiple ways to get the API key with priority order
        String apiKey = environment.getProperty("OPENAI_API_KEY"); // Highest priority - system env var
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = environment.getProperty("openai.api.key"); // application.yml property
        }
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-openai-key-here")) {
            apiKey = "your-openai-key-here"; // Final fallback
            log.warn("OpenAI API key not configured. Using placeholder. Please set OPENAI_API_KEY environment variable.");
        }
        return apiKey;
    }

    /**
     * Retrieves the OpenAI API base URL with fallback to default
     * @return OpenAI API base URL
     */
    private String getOpenAIBaseUrl() {
        return environment.getProperty("openai.api.base-url", "https://api.openai.com/v1");
    }

    /**
     * Sends a message to OpenAI Chat API and returns the AI response
     * @param chatRequest Contains message, system prompt, and conversation history
     * @return ChatResponse with AI response and usage data
     */
    public ChatResponse chatWithAI(ChatRequest chatRequest) {
        try {
            String apiKey = getOpenAIApiKey();
            String baseUrl = getOpenAIBaseUrl();

            // Validate API key configuration
            if (apiKey.equals("your-openai-key-here")) {
                throw new RuntimeException("OpenAI API key not configured. Please set OPENAI_API_KEY environment variable.");
            }

            log.debug("Sending chat request to OpenAI with message: {}",
                    chatRequest.getMessage().substring(0, Math.min(50, chatRequest.getMessage().length())) + "...");

            // Build conversation messages for OpenAI API
            List<Map<String, String>> messages = new ArrayList<>();

            // Add system prompt (default if not provided)
            String systemPrompt = chatRequest.getSystemPrompt() != null ?
                    chatRequest.getSystemPrompt() :
                    "You are a helpful assistant for Kwetu Hub, a youth employment platform in Uganda. " +
                            "Provide concise, helpful responses tailored for youth seeking opportunities.";
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // Add conversation history if available
            if (chatRequest.getConversationHistory() != null) {
                for (Map<String, String> msg : chatRequest.getConversationHistory()) {
                    messages.add(Map.of("role", msg.get("role"), "content", msg.get("content")));
                }
            }

            // Add current user message
            messages.add(Map.of("role", "user", "content", chatRequest.getMessage()));

            // Prepare OpenAI API request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini"); // Fast and cost-effective model
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 200); // Keep responses concise
            requestBody.put("temperature", 0.7); // Balanced creativity
            requestBody.put("presence_penalty", 0.1); // Slightly discourage repetition
            requestBody.put("frequency_penalty", 0.1); // Slightly discourage frequent words

            // Prepare HTTP headers with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call to OpenAI
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Process successful response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String aiResponse = (String) message.get("content");

                    // Create and return response DTO
                    ChatResponse chatResponse = new ChatResponse();
                    chatResponse.setResponse(aiResponse);
                    chatResponse.setUsage((Map<String, Object>) responseBody.get("usage"));

                    log.debug("Successfully received AI response");
                    return chatResponse;
                }
            }

            throw new RuntimeException("Failed to get valid response from OpenAI API");

        } catch (Exception e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
            throw new RuntimeException("AI service temporarily unavailable. Please try again later.");
        }
    }

    /**
     * Converts text to speech using OpenAI TTS API
     * @param text Text to convert to speech
     * @param voice Voice type (alloy, echo, fable, onyx, nova, shimmer)
     * @return Base64 encoded audio content
     */
    public String textToSpeech(String text, String voice) {
        try {
            String apiKey = getOpenAIApiKey();
            String baseUrl = getOpenAIBaseUrl();

            if (apiKey.equals("your-openai-key-here")) {
                throw new RuntimeException("OpenAI API key not configured.");
            }

            log.debug("Converting text to speech: {}", text.substring(0, Math.min(30, text.length())) + "...");

            // Prepare TTS request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "tts-1"); // Fast model for real-time use
            requestBody.put("input", text);
            requestBody.put("voice", voice != null ? voice : "alloy"); // Default to friendly voice
            requestBody.put("response_format", "mp3");
            requestBody.put("speed", 0.9); // Slightly slower for clarity

            // Prepare HTTP request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call OpenAI TTS API
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    baseUrl + "/audio/speech",
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            // Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String base64Audio = Base64.getEncoder().encodeToString(response.getBody());
                log.debug("Successfully generated speech audio");
                return base64Audio;
            }

            throw new RuntimeException("Failed to generate speech from OpenAI TTS API");

        } catch (Exception e) {
            log.error("OpenAI TTS error: {}", e.getMessage(), e);
            throw new RuntimeException("Text-to-speech service unavailable. Please try again later.");
        }
    }

    /**
     * Converts speech audio to text using OpenAI Whisper API
     * @param audioData Base64 encoded audio data
     * @return Transcribed text
     */
    public String speechToText(String audioData) {
        try {
            String apiKey = getOpenAIApiKey();
            String baseUrl = getOpenAIBaseUrl();

            if (apiKey.equals("your-openai-key-here")) {
                throw new RuntimeException("OpenAI API key not configured.");
            }

            log.debug("Converting speech to text");

            // Decode base64 audio data
            byte[] audioBytes = Base64.getDecoder().decode(audioData);

            // Prepare multipart form data for Whisper API
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create multipart request body
            org.springframework.util.MultiValueMap<String, Object> body =
                    new org.springframework.util.LinkedMultiValueMap<>();

            // Add audio file part
            org.springframework.core.io.ByteArrayResource audioResource =
                    new org.springframework.core.io.ByteArrayResource(audioBytes) {
                        @Override
                        public String getFilename() {
                            return "audio.webm";
                        }
                    };

            body.add("file", audioResource);
            body.add("model", "whisper-1");
            body.add("language", "en"); // Primary language with fallback for local languages

            HttpEntity<org.springframework.util.MultiValueMap<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            // Call OpenAI Whisper API
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/audio/transcriptions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String transcribedText = (String) response.getBody().get("text");
                log.debug("Successfully transcribed audio to text: {}", transcribedText);
                return transcribedText;
            }

            throw new RuntimeException("Failed to transcribe audio using OpenAI Whisper API");

        } catch (Exception e) {
            log.error("OpenAI Whisper error: {}", e.getMessage(), e);
            throw new RuntimeException("Speech-to-text service unavailable. Please try again later.");
        }
    }
}