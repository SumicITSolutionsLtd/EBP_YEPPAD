package com.youthconnect.ussd_service.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Model class representing a USSD session with expiration support.
 *
 * @author YouthConnect Uganda Development Team
 * @version 2.0.0
 */
@Data
public class UssdSession {
    private String sessionId;
    private String phoneNumber;
    private String currentMenu;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

    // User registration data
    private String userName;
    private String userGender;
    private String userAgeGroup;
    private String userDistrict;
    private String userBusinessStage;

    public UssdSession(String sessionId, String phoneNumber) {
        this.sessionId = sessionId;
        this.phoneNumber = phoneNumber;
        this.currentMenu = "welcome";
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateLastUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if the session has expired.
     *
     * @param timeoutMinutes the timeout in minutes
     * @return true if the session has expired
     */
    public boolean isExpired(int timeoutMinutes) {
        return ChronoUnit.MINUTES.between(lastUpdated, LocalDateTime.now()) > timeoutMinutes;
    }

    /**
     * ✓ FIXED: Clears all registration data from the session.
     * Should be called after successful registration.
     */
    public void clearRegistrationData() {
        this.userName = null;
        this.userGender = null;
        this.userAgeGroup = null;
        this.userDistrict = null;
        this.userBusinessStage = null;
    }

    /**
     * Checks if registration data is complete.
     *
     * @return true if all required fields are filled
     */
    public boolean hasCompleteRegistrationData() {
        return userName != null && !userName.isEmpty() &&
                userGender != null && !userGender.isEmpty() &&
                userAgeGroup != null && !userAgeGroup.isEmpty() &&
                userDistrict != null && !userDistrict.isEmpty() &&
                userBusinessStage != null && !userBusinessStage.isEmpty();
    }
}