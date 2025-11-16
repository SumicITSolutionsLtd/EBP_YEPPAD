package com.youthconnect.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * Password Encoder Configuration
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Location: backend/auth-service/src/main/java/com/youthconnect/auth_service/config/
 * Purpose: Provides PasswordEncoder bean in a separate configuration class
 *          to break circular dependency between SecurityConfig and AuthService
 *
 * PROBLEM SOLVED:
 * - SecurityConfig needs OAuth2SuccessHandler
 * - OAuth2SuccessHandler needs AuthService
 * - AuthService needs PasswordEncoder (was defined in SecurityConfig)
 * - This creates a circular dependency!
 *
 * SOLUTION:
 * - Extract PasswordEncoder to this separate config class
 * - Now AuthService can inject PasswordEncoder without depending on SecurityConfig
 * - Circular dependency is broken! ✅
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-11-16
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Password Encoder Bean
     *
     * BCrypt is a strong, adaptive hashing function designed specifically
     * for password storage. It includes several security features:
     *
     * 1. **Automatic Salting**: Each password gets a unique random salt,
     *    preventing rainbow table attacks
     *
     * 2. **Adaptive Work Factor**: The strength parameter (12) determines
     *    the computational cost. Higher = slower but more secure.
     *    - Strength 10 = ~100ms per hash
     *    - Strength 12 = ~250ms per hash (recommended)
     *    - Strength 14 = ~1 second per hash
     *
     * 3. **Future-Proof**: As computers get faster, you can increase the
     *    work factor without changing the algorithm
     *
     * WHY STRENGTH = 12?
     * - Good balance between security and performance
     * - OWASP recommended minimum is 10, we use 12 for extra security
     * - Fast enough for login (250ms) but slow enough to prevent brute force
     * - Can handle 3-4 logins per second per server
     *
     * SECURITY NOTES:
     * - BCrypt truncates passwords longer than 72 bytes (not characters!)
     * - BCrypt is resistant to GPU-based attacks (unlike SHA)
     * - The output format includes: algorithm, cost, salt, and hash
     *   Example: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
     *
     * @return PasswordEncoder BCrypt password encoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}