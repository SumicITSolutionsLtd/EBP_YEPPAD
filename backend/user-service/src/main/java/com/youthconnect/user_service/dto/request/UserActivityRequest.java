package com.youthconnect.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for tracking user activities for AI-driven recommendations.
 * <p>
 * Sent to the AI Recommendation Service for behavior analysis and personalization.
 * This request captures the user's interactions with platform content such as opportunities,
 * learning modules, posts, or audio sessions.
 * </p>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><b>userId</b> — must not be null</li>
 *   <li><b>activityType</b> — must not be blank (e.g., VIEW_OPPORTUNITY, APPLY_JOB)</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {
 *   "userId": 12345,
 *   "activityType": "VIEW_OPPORTUNITY",
 *   "targetId": 9876,
 *   "targetType": "OPPORTUNITY",
 *   "sessionId": "abcd-1234-xyz",
 *   "platform": "MOBILE",
 *   "metadata": {
 *       "duration": "30s",
 *       "source": "homepage"
 *   },
 *   "timestamp": "2025-10-05T12:30:45"
 * }
 * </pre>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityRequest {

    /**
     * User ID performing the activity.
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Type of activity performed by the user.
     * Examples: VIEW_OPPORTUNITY, APPLY_JOB, LISTEN_AUDIO, POST_QUESTION.
     */
    @NotBlank(message = "Activity type is required")
    private String activityType;

    /**
     * ID of the target resource (optional).
     * Examples: opportunity ID, module ID, post ID.
     */
    private Long targetId;

    /**
     * Type of the target resource (optional).
     * Examples: OPPORTUNITY, LEARNING_MODULE, POST, MENTOR.
     */
    private String targetType;

    /**
     * Session ID used to group user activities within a single session.
     */
    private String sessionId;

    /**
     * Platform used to perform the activity.
     * Valid values: WEB, MOBILE, USSD.
     */
    private String platform;

    /**
     * Additional metadata providing extra context for the activity.
     * For example: {"duration": "30s", "page": "opportunities"}
     */
    private Map<String, Object> metadata;

    /**
     * Timestamp of when the activity occurred.
     * Optional — defaults to current time if not provided.
     */
    private LocalDateTime timestamp;
}
