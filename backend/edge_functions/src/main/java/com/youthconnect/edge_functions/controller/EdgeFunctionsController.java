package com.youthconnect.edge_functions.controller;

import com.youthconnect.edge_functions.dto.*;
import com.youthconnect.edge_functions.dto.response.ChatResponse;
import com.youthconnect.edge_functions.service.OpenAIService;
import com.youthconnect.edge_functions.service.USSDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/edge")
@RequiredArgsConstructor
public class EdgeFunctionsController {

    private final OpenAIService openAIService;
    private final USSDService ussdService;

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithAI(@RequestBody ChatRequest chatRequest) {
        try {
            ChatResponse response = openAIService.chatWithAI(chatRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("response", "I'm having trouble right now. You can still use all the app features!");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/text-to-speech")
    public ResponseEntity<?> textToSpeech(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String voice = request.get("voice");

            if (text == null || text.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }

            String audioContent = openAIService.textToSpeech(text, voice);
            return ResponseEntity.ok(Map.of("audioContent", audioContent));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/voice-to-text")
    public ResponseEntity<?> voiceToText(@RequestBody Map<String, String> request) {
        try {
            String audioBase64 = request.get("audio");

            if (audioBase64 == null || audioBase64.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No audio data provided"));
            }

            byte[] audioData = Base64.getDecoder().decode(audioBase64);
            String transcribedText = openAIService.speechToText(audioData);

            return ResponseEntity.ok(Map.of("text", transcribedText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/ussd")
    public ResponseEntity<String> handleUSSD(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String phoneNumber = request.get("phoneNumber");
            String text = request.get("text");

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return ResponseEntity.badRequest().body("END Phone number is required");
            }

            String response = ussdService.handleUSSDRequest(sessionId, phoneNumber, text);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("END Service temporarily unavailable. Please try again later.");
        }
    }

    @PostMapping("/sms")
    public ResponseEntity<?> handleSMS(@RequestBody Map<String, String> request) {
        try {
            String from = request.get("from");
            String to = request.get("to");
            String text = request.get("text");

            // This would integrate with your SMS gateway
            // For now, return a mock response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("response", "SMS processed successfully");
            response.put("messageId", "sms_" + System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Service temporarily unavailable");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
