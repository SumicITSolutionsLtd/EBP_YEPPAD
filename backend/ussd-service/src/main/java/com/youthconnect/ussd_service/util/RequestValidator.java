package com.youthconnect.ussd_service.util;

import com.youthconnect.ussd_service.controller.UssdController.UssdRequestDTO;
import com.youthconnect.ussd_service.exception.UssdSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Request validator for USSD requests with comprehensive validation logic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final SecurityUtil securityUtil;

    /**
     * Validates Africa's Talking callback parameters.
     */
    public void validateAtCallbackParams(String sessionId, String serviceCode,
                                         String phoneNumber, String text,
                                         HttpServletRequest request) throws UssdSecurityException {

        // Validate request size
        long contentLength = request.getContentLengthLong();
        if (!securityUtil.isValidRequestSize(contentLength)) {
            throw new UssdSecurityException("Request content length exceeds maximum allowed size");
        }

        // Validate headers
        if (!securityUtil.hasValidHeaders(request)) {
            throw new UssdSecurityException("Invalid or suspicious request headers");
        }

        // Validate session ID
        if (!securityUtil.isValidSessionId(sessionId)) {
            throw new UssdSecurityException("Invalid session ID format");
        }

        // Validate service code
        if (!securityUtil.isValidServiceCode(serviceCode)) {
            throw new UssdSecurityException("Invalid service code format");
        }

        // Validate phone number
        if (!securityUtil.isValidPhoneNumber(phoneNumber)) {
            throw new UssdSecurityException("Invalid phone number format");
        }

        // Text can be null for initial requests, but if present, validate length
        if (text != null && text.length() > 500) {
            throw new UssdSecurityException("Text input exceeds maximum allowed length");
        }
    }

    /**
     * Validates USSD request DTO.
     */
    public void validateUssdRequest(UssdRequestDTO requestDto, HttpServletRequest request) throws UssdSecurityException {
        if (requestDto == null) {
            throw new UssdSecurityException("Request DTO cannot be null");
        }

        // Validate request size
        long contentLength = request.getContentLengthLong();
        if (!securityUtil.isValidRequestSize(contentLength)) {
            throw new UssdSecurityException("Request content length exceeds maximum allowed size");
        }

        // Validate headers
        if (!securityUtil.hasValidHeaders(request)) {
            throw new UssdSecurityException("Invalid or suspicious request headers");
        }

        // Validate session ID
        if (!StringUtils.hasText(requestDto.getSessionId()) ||
                !securityUtil.isValidSessionId(requestDto.getSessionId())) {
            throw new UssdSecurityException("Invalid session ID format");
        }

        // Validate phone number
        if (!StringUtils.hasText(requestDto.getPhoneNumber()) ||
                !securityUtil.isValidPhoneNumber(requestDto.getPhoneNumber())) {
            throw new UssdSecurityException("Invalid phone number format");
        }

        // Validate service code if present
        if (StringUtils.hasText(requestDto.getServiceCode()) &&
                !securityUtil.isValidServiceCode(requestDto.getServiceCode())) {
            throw new UssdSecurityException("Invalid service code format");
        }

        // Text validation
        if (requestDto.getText() != null && requestDto.getText().length() > 500) {
            throw new UssdSecurityException("Text input exceeds maximum allowed length");
        }
    }
}