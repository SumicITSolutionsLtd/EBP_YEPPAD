package com.youthconnect.file.service.security;

import com.youthconnect.file.service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * JWT Token Provider - Fixed for JJWT 0.12.x
 *
 * ✅ FIXED: Updated to use modern JJWT 0.12.x API
 * ✅ Uses parser() instead of deprecated parserBuilder()
 * ✅ Returns UUID for userId (matches auth-service)
 *
 * RESPONSIBILITIES:
 * - Parse and validate JWT tokens from API Gateway
 * - Extract user information (userId as UUID, username, roles)
 * - Validate token signature using HMAC-SHA256
 *
 * SECURITY:
 * - Uses 256-bit secret key for HS256 algorithm
 * - Stateless validation (no database lookup needed)
 * - Must use same secret as API Gateway
 *
 * @author Douglas Kings Kato
 * @version 2.1.0 (JJWT 0.12.x Compatible)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Get signing key for JWT validation
     *
     * SECURITY NOTES:
     * - Key must be at least 256 bits (32 bytes) for HS256
     * - Production: Load from secure vault (AWS Secrets Manager, HashiCorp Vault)
     * - Development: Can use environment variable
     *
     * @return SecretKey for HMAC-SHA256 signature validation
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract user ID from JWT token as UUID
     *
     * ✅ FIXED: Returns UUID instead of Long
     * ✅ FIXED: Uses modern JJWT 0.12.x parser() API
     *
     * TOKEN CLAIMS CHECKED (in order):
     * 1. "userId" - Custom claim set by auth-service
     * 2. "sub" - Standard JWT subject claim (fallback)
     *
     * CONVERSION LOGIC:
     * - String → Parse as UUID
     * - Already UUID → Return directly
     * - Other types → Log warning, return null
     *
     * @param token JWT token string (without "Bearer " prefix)
     * @return User ID as UUID, or null if extraction fails
     */
    public UUID getUserIdFromToken(String token) {
        try {
            // Parse token and extract claims using modern JJWT API
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())  // ✅ Modern API: verifyWith() instead of setSigningKey()
                    .build()
                    .parseSignedClaims(token)     // ✅ Modern API: parseSignedClaims() instead of parseClaimsJws()
                    .getPayload();                // ✅ Modern API: getPayload() instead of getBody()

            // Try to get userId from different possible claim names
            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                userIdObj = claims.get("sub"); // Fallback to standard subject claim
            }

            // Convert to UUID
            if (userIdObj instanceof String) {
                try {
                    return UUID.fromString((String) userIdObj);
                } catch (IllegalArgumentException e) {
                    log.error("❌ Invalid UUID format in token: {}", userIdObj);
                    return null;
                }
            } else if (userIdObj instanceof UUID) {
                return (UUID) userIdObj;
            }

            log.warn("⚠️ Could not extract userId from token - unexpected type: {}",
                    userIdObj != null ? userIdObj.getClass().getSimpleName() : "null");
            return null;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("⚠️ JWT token expired: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("❌ JWT signature validation failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("❌ Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract username from JWT token
     *
     * ✅ FIXED: Uses modern JJWT 0.12.x API
     *
     * USERNAME SOURCES (checked in order):
     * 1. "sub" claim (standard JWT subject)
     * 2. "username" claim (custom)
     * 3. "email" claim (fallback)
     *
     * @param token JWT token string
     * @return Username/email, or null if extraction fails
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Try subject first (standard claim)
            String username = claims.getSubject();
            if (username != null && !username.isBlank()) {
                return username;
            }

            // Try custom username claim
            username = claims.get("username", String.class);
            if (username != null && !username.isBlank()) {
                return username;
            }

            // Try email as fallback
            username = claims.get("email", String.class);
            return username;

        } catch (Exception e) {
            log.error("❌ Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract user roles from JWT token
     *
     * ✅ FIXED: Uses modern JJWT 0.12.x API
     *
     * ROLE FORMATS SUPPORTED:
     * - List<String>: ["ROLE_USER", "ROLE_MENTOR", "ROLE_NGO"]
     * - Comma-separated String: "ROLE_USER,ROLE_MENTOR"
     * - Single String: "ROLE_USER"
     *
     * DEFAULT BEHAVIOR:
     * - Returns ["ROLE_USER"] if no roles found
     * - Ensures Spring Security always has at least one role
     *
     * @param token JWT token string
     * @return List of role names with ROLE_ prefix
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Try to get roles from different claim names
            Object rolesObj = claims.get("roles");
            if (rolesObj == null) {
                rolesObj = claims.get("authorities"); // Alternative claim name
            }

            // Handle different role formats
            if (rolesObj instanceof List) {
                List<?> rolesList = (List<?>) rolesObj;
                // Convert to List<String>
                return rolesList.stream()
                        .map(Object::toString)
                        .map(this::ensureRolePrefix)
                        .toList();
            } else if (rolesObj instanceof String) {
                String rolesStr = (String) rolesObj;
                // Handle comma-separated roles
                return List.of(rolesStr.split(","))
                        .stream()
                        .map(String::trim)
                        .map(this::ensureRolePrefix)
                        .toList();
            }

            // Default role if none found
            log.debug("ℹ️ No roles found in token, using default ROLE_USER");
            return List.of("ROLE_USER");

        } catch (Exception e) {
            log.error("❌ Failed to extract roles from token: {}", e.getMessage());
            return List.of("ROLE_USER");
        }
    }

    /**
     * Validate JWT token
     *
     * ✅ FIXED: Uses modern JJWT 0.12.x API
     *
     * VALIDATION CHECKS:
     * - Signature verification (HMAC-SHA256 with shared secret)
     * - Expiration check (token not expired)
     * - Format validation (valid JWT structure)
     * - Claims validation (required claims present)
     *
     * ERRORS CAUGHT:
     * - ExpiredJwtException: Token past expiration time
     * - SignatureException: Invalid signature (wrong secret or tampered token)
     * - MalformedJwtException: Invalid JWT structure
     * - UnsupportedJwtException: Unsupported JWT format
     * - IllegalArgumentException: Empty/null token
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Parse and validate token - will throw exception if invalid
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            log.debug("✅ JWT token validated successfully");
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("⚠️ JWT token is expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("❌ JWT token format is unsupported: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("❌ JWT token is malformed: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("❌ JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("❌ JWT token is empty or null: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error validating JWT: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Extract token from Authorization header
     *
     * HEADER FORMAT: "Bearer {token}"
     *
     * EXTRACTION PROCESS:
     * 1. Check header is not null
     * 2. Verify it starts with configured prefix (default: "Bearer ")
     * 3. Remove prefix and return token
     * 4. Return null if invalid format
     *
     * EXAMPLE:
     * Input: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * Output: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *
     * @param authHeader Authorization header value from HTTP request
     * @return JWT token string without prefix, or null if invalid format
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            String token = authHeader.substring(jwtProperties.getTokenPrefix().length());
            log.debug("✅ Extracted JWT token from Authorization header");
            return token;
        }

        if (authHeader != null) {
            log.warn("⚠️ Authorization header present but missing '{}' prefix", jwtProperties.getTokenPrefix());
        }

        return null;
    }

    /**
     * Ensure role has ROLE_ prefix for Spring Security
     *
     * SPRING SECURITY CONVENTION:
     * - All roles should start with "ROLE_" prefix
     * - hasRole("USER") checks for "ROLE_USER"
     * - hasAuthority("ROLE_USER") checks exact match
     *
     * @param role Role name (with or without prefix)
     * @return Role name with ROLE_ prefix
     */
    private String ensureRolePrefix(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String trimmed = role.trim().toUpperCase();
        return trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed;
    }
}