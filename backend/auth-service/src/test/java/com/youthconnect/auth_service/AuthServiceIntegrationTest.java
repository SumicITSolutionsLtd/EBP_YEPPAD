package com.youthconnect.auth_service;

import com.youthconnect.auth_service.client.NotificationServiceClient;
import com.youthconnect.auth_service.client.UserServiceClient;
import com.youthconnect.auth_service.dto.request.LoginRequest;
import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.AuthResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import com.youthconnect.auth_service.entity.RefreshToken;
import com.youthconnect.auth_service.exception.InvalidCredentialsException;
import com.youthconnect.auth_service.repository.RefreshTokenRepository;
import com.youthconnect.auth_service.service.AuthService;
import com.youthconnect.auth_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================================
 * Auth Service Integration Tests
 * ============================================================================
 *
 * Tests registration, login, token refresh, logout, and related flows.
 * Uses @SpringBootTest for full application context with mocked Feign clients.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private NotificationServiceClient notificationServiceClient;

    private RegisterRequest testRegisterRequest;
    private LoginRequest testLoginRequest;
    private UserInfoResponse mockUserInfo;
    private UUID testUserId;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        testUserId = UUID.randomUUID();
        encodedPassword = passwordEncoder.encode("Test@123");

        // Prepare registration request
        testRegisterRequest = new RegisterRequest();
        testRegisterRequest.setEmail("test@youthconnect.ug");
        testRegisterRequest.setPhoneNumber("+256700000001");
        testRegisterRequest.setPassword("Test@123");
        testRegisterRequest.setRole("YOUTH");
        testRegisterRequest.setFirstName("Test");
        testRegisterRequest.setLastName("User");

        // Prepare login request
        testLoginRequest = new LoginRequest();
        testLoginRequest.setIdentifier("test@youthconnect.ug");
        testLoginRequest.setPassword("Test@123");

        // Prepare mock user info
        mockUserInfo = new UserInfoResponse();
        mockUserInfo.setUserId(testUserId);
        mockUserInfo.setEmail("test@youthconnect.ug");
        mockUserInfo.setPhoneNumber("+256700000001");
        mockUserInfo.setPasswordHash(encodedPassword);
        mockUserInfo.setRole("YOUTH");
        mockUserInfo.setActive(true);
    }

    // =========================== USER REGISTRATION TESTS ===========================
    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register new user with valid data")
        void testSuccessfulRegistration() {
            ApiResponse<UserInfoResponse> mockResponse = new ApiResponse<>();
            mockResponse.setSuccess(true);
            mockResponse.setMessage("User registered successfully");
            mockResponse.setData(mockUserInfo);

            when(userServiceClient.registerUser(any(RegisterRequest.class))).thenReturn(mockResponse);
            when(notificationServiceClient.sendWelcomeEmail(anyString(), anyString()))
                    .thenReturn(new ApiResponse<>());

            AuthResponse response = authService.register(testRegisterRequest);

            assertNotNull(response);
            assertNotNull(response.getAccessToken());
            assertNotNull(response.getRefreshToken());
            assertEquals("YOUTH", response.getRole());
            assertEquals(testUserId, response.getUserId());

            verify(userServiceClient, times(1)).registerUser(any(RegisterRequest.class));
            verify(notificationServiceClient, times(1)).sendWelcomeEmail(anyString(), anyString());

            Optional<RefreshToken> storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(response.getRefreshToken());
            assertTrue(storedToken.isPresent());
            assertEquals(testUserId, storedToken.get().getUserId());
        }

        @Test
        @DisplayName("Should fail registration with duplicate email")
        void testRegistrationWithDuplicateEmail() {
            ApiResponse<UserInfoResponse> mockResponse = new ApiResponse<>();
            mockResponse.setSuccess(false);
            mockResponse.setMessage("Email already exists");

            when(userServiceClient.registerUser(any(RegisterRequest.class))).thenReturn(mockResponse);

            assertThrows(RuntimeException.class, () -> authService.register(testRegisterRequest));
            verify(notificationServiceClient, never()).sendWelcomeEmail(anyString(), anyString());
        }
    }

    // ============================== LOGIN TESTS ==============================
    @Nested
    @DisplayName("User Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with valid email and password")
        void testSuccessfulLoginWithEmail() {
            ApiResponse<UserInfoResponse> mockResponse = new ApiResponse<>();
            mockResponse.setSuccess(true);
            mockResponse.setData(mockUserInfo);

            when(userServiceClient.getUserByIdentifier(anyString())).thenReturn(mockResponse);

            AuthResponse response = authService.login(testLoginRequest);

            assertNotNull(response);
            assertEquals("YOUTH", response.getRole());
            verify(userServiceClient, times(1)).getUserByIdentifier(anyString());
        }

        @Test
        @DisplayName("Should fail login with invalid password")
        void testLoginWithInvalidPassword() {
            ApiResponse<UserInfoResponse> mockResponse = new ApiResponse<>();
            mockResponse.setSuccess(true);
            mockResponse.setData(mockUserInfo);

            when(userServiceClient.getUserByIdentifier(anyString())).thenReturn(mockResponse);

            testLoginRequest.setPassword("WrongPassword");
            assertThrows(InvalidCredentialsException.class, () -> authService.login(testLoginRequest));
        }
    }

    // ============================== TOKEN REFRESH TESTS ==============================
    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        private String validRefreshToken;

        @BeforeEach
        void setUpRefreshToken() {
            RefreshToken token = new RefreshToken();
            token.setUserId(testUserId);
            token.setUserEmail("test@youthconnect.ug");
            token.setUserRole("YOUTH");
            token.setToken(UUID.randomUUID().toString());
            token.setExpiresAt(LocalDateTime.now().plusDays(7));
            token.setRevoked(false);
            token.setCreatedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
            validRefreshToken = token.getToken();

            ApiResponse<UserInfoResponse> mockResponse = new ApiResponse<>();
            mockResponse.setSuccess(true);
            mockResponse.setData(mockUserInfo);

            when(userServiceClient.getUserById(any(UUID.class))).thenReturn(mockResponse);
        }

        @Test
        @DisplayName("Should successfully refresh access token")
        void testSuccessfulTokenRefresh() {
            AuthResponse response = authService.refreshToken(validRefreshToken);
            assertNotNull(response);
            assertNotNull(response.getAccessToken());
            assertNotEquals(validRefreshToken, response.getRefreshToken());
        }
    }
}
