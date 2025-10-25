package com.youthconnect.notification.service.exception;

import lombok.Getter;

/**
 * Custom exception for notification-related errors.
 */
@Getter
public class NotificationException extends RuntimeException {

    private final String errorCode;
    private final String recipient;

    public NotificationException(String message) {
        super(message);
        this.errorCode = "NOTIFICATION_ERROR";
        this.recipient = null;
    }

    public NotificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.recipient = null;
    }

    public NotificationException(String message, String errorCode, String recipient) {
        super(message);
        this.errorCode = errorCode;
        this.recipient = recipient;
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "NOTIFICATION_ERROR";
        this.recipient = null;
    }
}