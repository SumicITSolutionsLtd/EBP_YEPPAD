package com.youthconnect.job_services.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT Authentication Filter
 *
 * Validates JWT tokens from the Authorization header and sets up
 * Spring Security authentication context.
 *
 * Process:
 * 1. Extract JWT from Authorization header (Bearer token)
 * 2. Validate token signature and expiration
 * 3. Extract user information (userId, role) from claims
 * 4. Set authentication in SecurityContext
 * 5. Make user info available to controllers via SecurityContext
 *
 * Token Claims Expected:
 * - sub: User email
 * - userId: UUID string
 * - role: User role (YOUTH, NGO, COMPANY, etc.)
 * - exp: Expiration timestamp
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    /**
     * Filter incoming requests and validate JWT tokens
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extract JWT from Authorization header
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && validateToken(jwt)) {
                // Extract user information from JWT
                Claims claims = extractClaims(jwt);
                String email = claims.getSubject();
                String userIdStr = claims.get("userId", String.class);
                String role = claims.get("role", String.class);

                // Convert userId string to UUID
                UUID userId = UUID.fromString(userIdStr);

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // Add custom details (userId, role)
                JwtUserDetails userDetails = new JwtUserDetails(userId, email, role);
                authentication.setDetails(userDetails);

                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authentication set for user: {} with role: {}", email, role);
            }
        } catch (Exception e) {
            log.error("Failed to authenticate user: {}", e.getMessage());
            // Don't set authentication - request will be rejected as unauthorized
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * Expected format: "Bearer <token>"
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Validate JWT token signature and expiration
     */
    private boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract claims from JWT token
     */
    private Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Custom user details to store in authentication
     */
    public static class JwtUserDetails {
        private final UUID userId;
        private final String email;
        private final String role;

        public JwtUserDetails(UUID userId, String email, String role) {
            this.userId = userId;
            this.email = email;
            this.role = role;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }
}
