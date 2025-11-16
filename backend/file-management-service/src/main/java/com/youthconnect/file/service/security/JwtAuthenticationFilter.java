package com.youthconnect.file.service.security;

import com.youthconnect.file.service.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
// ✅ FIXED: Correct Spring Security imports
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 *
 * ✅ GUIDELINES COMPLIANCE:
 * - Validates JWT tokens from API Gateway
 * - Extracts user ID as UUID (not Long)
 * - Sets authentication context for downstream processing
 * - Skips public endpoints
 *
 * FILTER EXECUTION ORDER:
 * 1. Extract JWT token from Authorization header
 * 2. Validate token signature and expiration
 * 3. Extract user information (UUID, username, roles)
 * 4. Set authentication in SecurityContext
 * 5. Continue filter chain
 *
 * PUBLIC ENDPOINTS (skipped):
 * - /api/files/health
 * - /api/files/download/public/**
 * - /api/files/download/modules/**
 * - /actuator/health
 * - /actuator/info
 * - /swagger-ui/**
 * - /v3/api-docs/**
 *
 * SECURITY CONTEXT:
 * - Principal: User's UUID (not username!)
 * - Credentials: null (stateless, token-based)
 * - Authorities: List of granted roles
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Fixed with Guidelines)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * Main filter logic - called once per request
     *
     * WORKFLOW:
     * 1. Check if endpoint is public (skip authentication)
     * 2. Extract token from Authorization header
     * 3. Validate token
     * 4. Extract user info (UUID, username, roles)
     * 5. Set authentication context
     * 6. Continue to next filter
     *
     * ERROR HANDLING:
     * - Invalid token: Log error, continue without authentication
     * - Missing token: Skip authentication (public endpoint or error)
     * - Expired token: Log warning, continue without authentication
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain to continue
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT token from Authorization header
            String authHeader = request.getHeader(jwtProperties.getHeaderString());
            String token = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // Step 2: Validate token if present
            if (token != null && jwtTokenProvider.validateToken(token)) {

                // Step 3: Extract user information from token
                UUID userId = jwtTokenProvider.getUserIdFromToken(token);  // ✅ UUID
                String username = jwtTokenProvider.getUsernameFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);

                if (userId != null && username != null) {
                    // Convert roles to Spring Security authorities
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Step 4: Create authentication token
                    // ✅ Principal is UUID (not Long) - important for @PreAuthorize checks
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,      // Principal (UUID)
                                    null,        // Credentials (not needed, stateless)
                                    authorities  // Granted authorities (roles)
                            );

                    // Set additional request details
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Step 5: Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("✅ Set authentication for user: {} (ID: {})", username, userId);
                }
            }

        } catch (Exception e) {
            log.error("❌ Cannot set user authentication: {}", e.getMessage());
            // Don't throw - let request continue without authentication
            // Protected endpoints will be blocked by Spring Security
        }

        // Step 6: Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Skip filter for public endpoints
     *
     * PERFORMANCE: Avoids unnecessary JWT validation for public endpoints
     *
     * PUBLIC ENDPOINTS:
     * - Health checks
     * - Public file downloads
     * - Learning modules
     * - Actuator endpoints
     * - Swagger documentation
     *
     * @param request HTTP request
     * @return true if filter should be skipped
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip filter for these public paths
        boolean shouldSkip = path.startsWith("/api/files/health") ||
                path.startsWith("/api/files/download/public/") ||
                path.startsWith("/api/files/download/modules/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/webjars/");

        if (shouldSkip) {
            log.debug("⏭️ Skipping JWT filter for public endpoint: {}", path);
        }

        return shouldSkip;
    }
}