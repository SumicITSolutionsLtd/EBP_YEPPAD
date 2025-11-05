package com.youthconnect.api_gateway.filter;

import com.youthconnect.api_gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for JWT Authentication Filter
 *
 * Tests various authentication scenarios:
 * - Public endpoints (no auth required)
 * - Valid JWT tokens
 * - Invalid JWT tokens
 * - Expired tokens
 * - Missing tokens
 * - User context header injection
 *
 * Location: api-gateway/src/test/java/com/youthconnect/api_gateway/filter/
 *
 * @author Youth Connect Development Team
 * @version 1.0.0
 */
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.token";
    private static final String INVALID_TOKEN = "invalid.token.here";
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final List<String> TEST_ROLES = List.of("USER", "MENTOR");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);

        // Mock filter chain to continue processing
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());
    }

    /**
     * Test 1: Public endpoint should not require authentication
     */
    @Test
    void testPublicEndpoint_ShouldAllowWithoutToken() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    /**
     * Test 2: Health check endpoint should not require authentication
     */
    @Test
    void testHealthEndpoint_ShouldAllowWithoutToken() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }

    /**
     * Test 3: OPTIONS request (CORS preflight) should pass without auth
     */
    @Test
    void testOptionsRequest_ShouldAllowWithoutToken() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .options("/api/users")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }

    /**
     * Test 4: Protected endpoint without token should return 401
     */
    @Test
    void testProtectedEndpoint_NoToken_ShouldReturn401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(filterChain, never()).filter(exchange);
    }

    /**
     * Test 5: Protected endpoint with invalid token format should return 401
     */
    @Test
    void testProtectedEndpoint_InvalidTokenFormat_ShouldReturn401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(filterChain, never()).filter(exchange);
    }

    /**
     * Test 6: Protected endpoint with valid token should pass
     */
    @Test
    void testProtectedEndpoint_ValidToken_ShouldPass() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock JWT validation
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtUtil.extractRoles(VALID_TOKEN)).thenReturn(TEST_ROLES);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil, times(1)).validateToken(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUserId(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractEmail(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractRoles(VALID_TOKEN);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    /**
     * Test 7: Valid token should inject user context headers
     */
    @Test
    void testValidToken_ShouldInjectUserContextHeaders() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock JWT validation
        when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtUtil.extractRoles(VALID_TOKEN)).thenReturn(TEST_ROLES);

        // Capture modified exchange
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenAnswer(invocation -> {
                    ServerWebExchange modifiedExchange = invocation.getArgument(0);
                    ServerHttpRequest modifiedRequest = modifiedExchange.getRequest();

                    // Verify user context headers were added
                    assert modifiedRequest.getHeaders().getFirst("X-User-Id").equals(TEST_USER_ID);
                    assert modifiedRequest.getHeaders().getFirst("X-User-Email").equals(TEST_EMAIL);
                    assert modifiedRequest.getHeaders().getFirst("X-User-Roles").equals("USER,MENTOR");
                    assert modifiedRequest.getHeaders().getFirst("X-Auth-Token").equals(VALID_TOKEN);

                    return Mono.empty();
                });

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    /**
     * Test 8: Invalid token should return 401
     */
    @Test
    void testProtectedEndpoint_InvalidToken_ShouldReturn401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock JWT validation failure
        when(jwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(jwtUtil, times(1)).validateToken(INVALID_TOKEN);
        verify(filterChain, never()).filter(exchange);
    }

    /**
     * Test 9: Exception during token validation should return 401
     */
    @Test
    void testTokenValidation_ThrowsException_ShouldReturn401() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock exception during validation
        when(jwtUtil.validateToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Token parsing error"));

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(filterChain, never()).filter(exchange);
    }

    /**
     * Test 10: Filter order should be -80 (after rate limit, before logging)
     */
    @Test
    void testFilterOrder() {
        // Assert
        assert jwtAuthenticationFilter.getOrder() == -80;
    }
}