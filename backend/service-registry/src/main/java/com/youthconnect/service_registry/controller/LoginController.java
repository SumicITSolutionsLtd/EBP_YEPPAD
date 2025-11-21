package com.youthconnect.service_registry.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ================================================================================
 * Login Controller - Handles Custom Login Page Requests
 * ================================================================================
 *
 * This controller serves the custom login page for the Eureka Server dashboard.
 * It's responsible for:
 * - Displaying the login form
 * - Showing error messages for failed login attempts
 * - Showing success messages after logout
 *
 * Spring Security automatically handles the actual authentication process.
 * This controller only provides the UI.
 *
 * @author EBP Development Team
 * @version 1.0.0
 * @since 2025-01-29
 * ================================================================================
 */
@Slf4j
@Controller
public class LoginController {

    /**
     * Displays the login page.
     *
     * This endpoint handles the following scenarios:
     * 1. Initial login page request (no parameters)
     * 2. Failed login attempt (error=true)
     * 3. Successful logout (logout=true)
     *
     * URL Parameters:
     * - error: Set to "true" by Spring Security after failed authentication
     * - logout: Set to "true" by Spring Security after successful logout
     *
     * Model Attributes:
     * - error: Boolean flag indicating authentication failure
     * - logout: Boolean flag indicating successful logout
     * - errorMessage: Descriptive error message for failed login
     * - logoutMessage: Success message for logout
     *
     * @param error Optional parameter indicating authentication failure
     * @param logout Optional parameter indicating successful logout
     * @param model Spring MVC Model for passing data to view
     * @return View name (login.html in templates folder)
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // ════════════════════════════════════════════════════════════════
        // HANDLE AUTHENTICATION ERROR
        // ════════════════════════════════════════════════════════════════
        if (error != null) {
            log.warn("Login attempt failed - Invalid credentials provided");

            // Add error flag to model (used in login.html)
            model.addAttribute("error", true);

            // Add descriptive error message
            model.addAttribute("errorMessage",
                    "Invalid username or password. Please try again.");

            // Log security event (consider sending to SIEM in production)
            log.info("Failed login attempt detected from client");
        }

        // ════════════════════════════════════════════════════════════════
        // HANDLE SUCCESSFUL LOGOUT
        // ════════════════════════════════════════════════════════════════
        if (logout != null) {
            log.info("User successfully logged out");

            // Add logout flag to model (used in login.html)
            model.addAttribute("logout", true);

            // Add success message
            model.addAttribute("logoutMessage",
                    "You have been successfully logged out.");
        }

        // ════════════════════════════════════════════════════════════════
        // LOG PAGE ACCESS
        // ════════════════════════════════════════════════════════════════
        if (error == null && logout == null) {
            log.debug("Login page accessed");
        }

        // ════════════════════════════════════════════════════════════════
        // RETURN VIEW NAME
        // ════════════════════════════════════════════════════════════════
        // Spring will look for: src/main/resources/templates/login.html
        return "login";
    }

    /**
     * Optional: Custom access denied page.
     *
     * This endpoint handles cases where authenticated users try to access
     * resources they don't have permission for (403 Forbidden).
     *
     * Currently not used in basic Eureka setup, but useful if you add
     * role-based access control (RBAC) in the future.
     *
     * @param model Spring MVC Model
     * @return View name for access denied page
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        log.warn("Access denied - User attempted to access restricted resource");

        model.addAttribute("errorMessage",
                "You do not have permission to access this resource.");

        // Create access-denied.html if you want a custom 403 page
        // For now, redirect to login
        return "redirect:/login?error=access-denied";
    }

    /**
     * Optional: Handle session timeout.
     *
     * When a user's session expires, they can be redirected here
     * before being sent to the login page.
     *
     * @param model Spring MVC Model
     * @return Redirect to login page with timeout message
     */
    @GetMapping("/session-expired")
    public String sessionExpired(Model model) {
        log.info("User session expired");

        // Redirect to login with custom message
        return "redirect:/login?timeout=true";
    }

    /**
     * ════════════════════════════════════════════════════════════════════
     * IMPLEMENTATION NOTES
     * ════════════════════════════════════════════════════════════════════
     *
     * 1. VIEW RESOLUTION:
     *    - Spring Boot automatically configures Thymeleaf
     *    - View name "login" resolves to: src/main/resources/templates/login.html
     *    - Make sure login.html exists in that exact location
     *
     * 2. AUTHENTICATION FLOW:
     *    Step 1: User navigates to http://localhost:8761
     *    Step 2: Spring Security intercepts (not authenticated)
     *    Step 3: Redirects to /login (this controller)
     *    Step 4: This controller returns login.html
     *    Step 5: User submits form to POST /login
     *    Step 6: Spring Security validates credentials
     *    Step 7: Success → redirect to / (dashboard)
     *            Failure → redirect to /login?error=true (back to step 3)
     *
     * 3. SECURITY CONSIDERATIONS:
     *    - This controller is intentionally simple
     *    - Authentication logic is handled by Spring Security
     *    - Never implement your own authentication here
     *    - Don't log passwords or sensitive information
     *    - Consider adding CAPTCHA for production (after N failed attempts)
     *
     * 4. PRODUCTION ENHANCEMENTS:
     *    - Add rate limiting for login attempts
     *    - Implement account lockout after X failed attempts
     *    - Add multi-factor authentication (MFA/2FA)
     *    - Log failed login attempts to SIEM
     *    - Display last login time to user
     *    - Show "remember me" functionality (already in form)
     *
     * 5. INTERNATIONALIZATION (i18n):
     *    - Error messages currently hardcoded in English
     *    - For multi-language support, use Spring MessageSource:
     *      model.addAttribute("errorMessage",
     *          messageSource.getMessage("login.error", null, locale));
     *
     * ════════════════════════════════════════════════════════════════════
     */
}