package com.youthconnect.auth_service.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility Class - Complete Implementation
 *
 * Handles all JWT token operations for the Auth Service including:
 * - Access token generation (short-lived)
 * - Refresh token generation (long-lived)
 * - Token validation and parsing
 * - Claims extraction
 * - Token expiration checking
 *
 * Security Implementation:
 * - Uses HMAC-SHA256 algorithm (HS256)
 * - Secret key must be at least 256 bits (32 bytes)
 * - Tokens are signed to prevent tampering
 * - Includes issuer and audience claims for additional validation
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.issuer:youth-connect-auth-service}")
    private String issuer;

    @Value("${jwt.audience:youth-connect-platform}")
    private String audience;

    /**
     * Generate JWT Access Token
     *
     * Creates a short-lived access token containing user information.
     * This token is used for API authentication.
     *
     * @param username User's email or identifier
     * @param userId User's database ID
     * @param role User's role (YOUTH, NGO, MENTOR, etc.)
     * @return JWT access token string
     */
    public String generateAccessToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("type", "ACCESS");

        return createToken(claims, username, accessTokenExpiration);
    }

    /**
     * Generate JWT Refresh Token
     *
     * Creates a long-lived refresh token for obtaining new access tokens
     * without requiring the user to log in again.
     *
     * @param username User's email or identifier
     * @param userId User's database ID
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");

        return createToken(claims, username, refreshTokenExpiration);
    }

    /**
     * Create JWT Token (Internal Method)
     *
     * Core token creation logic used by both access and refresh token generators.
     *
     * @param claims Custom claims to include in token
     * @param subject Token subject (username)
     * @param expiration Token expiration time in milliseconds
     * @return Signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get Signing Key
     *
     * Converts the secret string into a SecretKey object for signing tokens.
     * The secret must be at least 256 bits (32 bytes) for HS256 algorithm.
     *
     * @return SecretKey for signing JWT tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract Username from Token
     *
     * @param token JWT token string
     * @return Username (subject) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract User ID from Token
     *
     * @param token JWT token string
     * @return User ID as Long
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract User Role from Token
     *
     * @param token JWT token string
     * @return User role as String
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract Token Type from Token
     *
     * @param token JWT token string
     * @return Token type (ACCESS or REFRESH)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extract Expiration Date from Token
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract Specific Claim from Token
     *
     * Generic method to extract any claim from the token using a function.
     *
     * @param token JWT token string
     * @param claimsResolver Function to extract specific claim
     * @param <T> Type of claim to extract
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract All Claims from Token
     *
     * Parses the token and returns all claims contained within.
     *
     * @param token JWT token string
     * @return All claims from token
     * @throws JwtException if token is invalid or expired
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if Token is Expired
     *
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Validate Access Token
     *
     * Validates that the token is valid, not expired, and matches the username.
     *
     * @param token JWT token string
     * @param username Username to validate against
     * @return true if token is valid, false otherwise
     */
    public Boolean validateAccessToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            final String tokenType = extractTokenType(token);

            return (tokenUsername.equals(username)
                    && !isTokenExpired(token)
                    && "ACCESS".equals(tokenType));
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate Refresh Token
     *
     * Validates that the token is a valid refresh token.
     *
     * @param token JWT token string
     * @return true if token is valid refresh token, false otherwise
     */
    public Boolean validateRefreshToken(String token) {
        try {
            final String tokenType = extractTokenType(token);
            return !isTokenExpired(token) && "REFRESH".equals(tokenType);
        } catch (JwtException e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get Token Expiration in Seconds
     *
     * @param token JWT token string
     * @return Seconds until token expires (negative if already expired)
     */
    public Long getTokenExpirationSeconds(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            return (expiration.getTime() - now.getTime()) / 1000;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Extract Complete User Info from Token
     *
     * @param token JWT token string
     * @return JwtUserInfo object containing all user information
     */
    public JwtUserInfo extractUserInfo(String token) {
        JwtUserInfo userInfo = new JwtUserInfo();
        userInfo.setUsername(extractUsername(token));
        userInfo.setUserId(extractUserId(token));
        userInfo.setRole(extractRole(token));
        userInfo.setTokenType(extractTokenType(token));
        userInfo.setExpiresAt(extractExpiration(token));
        userInfo.setExpiresInSeconds(getTokenExpirationSeconds(token));
        return userInfo;
    }

    /**
     * Data class for holding extracted JWT user information
     */
    @Data
    public static class JwtUserInfo {
        private String username;
        private Long userId;
        private String role;
        private String tokenType;
        private Date expiresAt;
        private Long expiresInSeconds;
    }
}