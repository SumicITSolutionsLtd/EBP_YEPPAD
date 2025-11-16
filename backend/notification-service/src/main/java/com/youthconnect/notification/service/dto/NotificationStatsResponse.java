package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION STATISTICS RESPONSE DTO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Response DTO for notification statistics endpoint.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {

    private Long totalSent;
    private Long totalFailed;
    private Long totalPending;
    private String successRate;

    private ChannelStats sms;
    private ChannelStats email;
    private ChannelStats push;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelStats {
        private Long sent;
        private Long failed;
        private String successRate;
    }
}