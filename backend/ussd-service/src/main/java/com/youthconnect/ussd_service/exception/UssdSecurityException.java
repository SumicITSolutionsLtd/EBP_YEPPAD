package com.youthconnect.ussd_service.exception;

/**
 * Custom exception class for USSD security-related violations and threats.
 * This exception is thrown when security validation fails, unauthorized access
 * is attempted, or potentially malicious activity is detected.
 *
 * Used throughout the USSD service to handle:
 * - Invalid request parameters
 * - Unauthorized IP addresses
 * - Malicious input patterns
 * - Rate limiting violations
 * - Authentication/authorization failures
 */
public class UssdSecurityException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String securityEvent;
    private final String clientInfo;

    /**
     * Constructs a new UssdSecurityException with the specified detail message.
     *
     * @param message the detail message explaining the security violation
     */
    public UssdSecurityException(String message) {
        super(message);
        this.securityEvent = null;
        this.clientInfo = null;
    }

    /**
     * Constructs a new UssdSecurityException with the specified detail message and cause.
     *
     * @param message the detail message explaining the security violation
     * @param cause the cause of the exception (which is saved for later retrieval)
     */
    public UssdSecurityException(String message, Throwable cause) {
        super(message, cause);
        this.securityEvent = null;
        this.clientInfo = null;
    }

    /**
     * Constructs a new UssdSecurityException with enhanced security context information.
     *
     * @param message the detail message explaining the security violation
     * @param securityEvent the type of security event that triggered this exception
     * @param clientInfo additional information about the client (IP, User-Agent, etc.)
     */
    public UssdSecurityException(String message, String securityEvent, String clientInfo) {
        super(message);
        this.securityEvent = securityEvent;
        this.clientInfo = clientInfo;
    }

    /**
     * Constructs a new UssdSecurityException with enhanced security context and cause.
     *
     * @param message the detail message explaining the security violation
     * @param cause the cause of the exception
     * @param securityEvent the type of security event that triggered this exception
     * @param clientInfo additional information about the client
     */
    public UssdSecurityException(String message, Throwable cause, String securityEvent, String clientInfo) {
        super(message, cause);
        this.securityEvent = securityEvent;
        this.clientInfo = clientInfo;
    }

    /**
     * Gets the security event type that triggered this exception.
     *
     * @return the security event type, or null if not specified
     */
    public String getSecurityEvent() {
        return securityEvent;
    }

    /**
     * Gets additional client information associated with this security violation.
     *
     * @return client information string, or null if not specified
     */
    public String getClientInfo() {
        return clientInfo;
    }

    /**
     * Returns a detailed string representation of this exception including
     * security context information if available.
     *
     * @return a string representation of this exception
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());

        String message = getMessage();
        if (message != null) {
            sb.append(": ").append(message);
        }

        if (securityEvent != null) {
            sb.append(" [Security Event: ").append(securityEvent).append("]");
        }

        if (clientInfo != null) {
            sb.append(" [Client Info: ").append(clientInfo).append("]");
        }

        return sb.toString();
    }
}