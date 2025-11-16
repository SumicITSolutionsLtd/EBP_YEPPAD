package com.youthconnect.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * JWT Utility Class for API Gateway
 *
 * Handles JWT token validation and extraction of claims.
 * This utility ONLY validates tokens - it does NOT generate them.
 * Token generation is handled by the auth-service.
 *
 * Responsibilities:
 * - Validate JWT signature using shared secret key
 * - Check token expiration
 * - Extract user information (userId, email, roles)
 * - Handle JWT parsing exceptions
 *
 * Security Notes:
 * - Uses HMAC-SHA256 algorithm for signature validation
 * - Secret key must match the one used by auth-service
 * - Tokens are stateless - no database lookup required
 *
 * Configuration:
 * - jwt.secret: Secret key shared with auth-service (min 256 bits)
 * - jwt.expiration: Not used in gateway (validation only)
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/util/
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * Secret key for JWT signature validation
     * CRITICAL: Must match the secret used by auth-service
     * Minimum 256 bits (32 characters) required for HS256
     */
    @Value("${jwt.secret:youth-connect-secure-secret-key-2025-minimum-256-bits-required-for-production}")
    private String secretKey;

    /**
     * Generate cryptographic key from secret string
     * Uses HMAC-SHA256 algorithm
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validate JWT token
     * Checks signature validity and expiration
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Parse token and verify signature
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            // Check if token is expired
            if (isTokenExpired(token)) {
                log.warn("JWT token has expired");
                return false;
            }

            log.debug("JWT token validated successfully");
            return true;

        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error validating JWT: {}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * Extract all claims from JWT token
     *
     * @param token JWT token string
     * @return Claims object containing all token data
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract specific claim using a resolver function
     * Generic method for extracting any claim type
     *
     * @param token JWT token string
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract user ID from token
     * User ID is stored as the JWT subject
     *
     * @param token JWT token string
     * @return User ID (UUID string)
     */
    public String extractUserId(String token) {
        String userId = extractClaim(token, Claims::getSubject);
        log.debug("Extracted userId: {}", userId);
        return userId;
    }

    /**
     * Extract user email from token
     * Email is stored as a custom claim
     *
     * @param token JWT token string
     * @return User email address
     */
    public String extractEmail(String token) {
        String email = extractClaim(token, claims -> claims.get("email", String.class));
        log.debug("Extracted email: {}", email);
        return email;
    }

    /**
     * Extract user roles from token
     * Roles are stored as a comma-separated string or list
     *
     * @param token JWT token string
     * @return List of role names (e.g., ["USER", "MENTOR"])
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);

        // Try to get roles as a list first
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List) {
            List<String> roles = (List<String>) rolesObj;
            log.debug("Extracted roles: {}", roles);
            return roles;
        } else if (rolesObj instanceof String) {
            // If stored as comma-separated string
            String rolesString = (String) rolesObj;
            List<String> roles = List.of(rolesString.split(","));
            log.debug("Extracted roles from string: {}", roles);
            return roles;
        }

        // Default to USER role if none found
        log.warn("No roles found in token, defaulting to USER");
        return List.of("USER");
    }

    /**
     * Extract token expiration date
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Check if token has expired
     *
     * @param token JWT token string
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean expired = expiration.before(new Date());

        if (expired) {
            log.debug("Token expired at: {}", expiration);
        }

        return expired;
    }

    /**
     * Extract token issued-at timestamp
     * Useful for debugging and audit trails
     *
     * @param token JWT token string
     * @return Issued-at date
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Get time remaining until token expiration (in seconds)
     * Useful for refresh token logic
     *
     * @param token JWT token string
     * @return Seconds until expiration (negative if already expired)
     */
    public long getTimeUntilExpiration(String token) {
        Date expiration = extractExpiration(token);
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        long secondsRemaining = (expirationTime - now) / 1000;

        log.debug("Token expires in {} seconds", secondsRemaining);
        return secondsRemaining;
    }

    /**
     * Check if token will expire soon (within threshold)
     * Useful for triggering token refresh warnings
     *
     * @param token JWT token string
     * @param thresholdSeconds Seconds before expiration to consider "soon"
     * @return true if expiring soon, false otherwise
     */
    public boolean isTokenExpiringSoon(String token, long thresholdSeconds) {
        long timeRemaining = getTimeUntilExpiration(token);
        return timeRemaining > 0 && timeRemaining <= thresholdSeconds;
    }

    /**
     * Extract username from token (if present)
     * Some systems use username instead of email
     *
     * @param token JWT token string
     * @return Username or null if not present
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, claims -> claims.get("username", String.class));
        } catch (Exception e) {
            log.debug("No username claim found in token");
            return null;
        }
    }

    /**
     * Extract custom claim by key
     * Allows extraction of any additional claims stored in token
     *
     * @param token JWT token string
     * @param claimKey Name of the custom claim
     * @return Claim value as string, or null if not found
     */
    public String extractCustomClaim(String token, String claimKey) {
        try {
            return extractClaim(token, claims -> claims.get(claimKey, String.class));
        } catch (Exception e) {
            log.debug("Custom claim '{}' not found in token", claimKey);
            return null;
        }
    }

    /**
     * Get detailed token information for debugging
     * NEVER expose this in production logs (contains sensitive data)
     *
     * @param token JWT token string
     * @return Formatted string with token details
     */
    public String getTokenDebugInfo(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return String.format(
                    "Token Info: userId=%s, email=%s, roles=%s, issued=%s, expires=%s",
                    extractUserId(token),
                    extractEmail(token),
                    extractRoles(token),
                    extractIssuedAt(token),
                    extractExpiration(token)
            );
        } catch (Exception e) {
            return "Unable to parse token: " + e.getMessage();
        }
    }
}