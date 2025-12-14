package com.ra.freshChickenAPI.scheduler;

import com.ra.freshChickenAPI.service.FCMTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FCMTokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FCMTokenCleanupScheduler.class);

    @Autowired
    private FCMTokenService fcmTokenService;

    /**
     * Clean up old inactive tokens every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldInactiveTokens() {
        try {
            logger.info("🧹 Starting cleanup of old inactive FCM tokens...");
            fcmTokenService.cleanupOldTokens(30); // Remove tokens inactive for 30+ days
            logger.info("✅ Old inactive FCM token cleanup completed");
        } catch (Exception e) {
            logger.error("❌ Error during old inactive token cleanup: {}", e.getMessage());
        }
    }

    /**
     * Validate and cleanup potentially invalid tokens every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void validateAndCleanupTokens() {
        try {
            logger.info("🔍 Starting FCM token validation and cleanup...");
            fcmTokenService.validateAndCleanupTokens();
            logger.info("✅ FCM token validation and cleanup completed");
        } catch (Exception e) {
            logger.error("❌ Error during token validation and cleanup: {}", e.getMessage());
        }
    }

    /**
     * Emergency cleanup for tokens not used in 7 days (runs every day at 3 AM)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void emergencyTokenCleanup() {
        try {
            logger.info("🚨 Starting emergency FCM token cleanup (7+ days old)...");
            fcmTokenService.cleanupOldTokens(7); // Remove tokens inactive for 7+ days
            logger.info("✅ Emergency FCM token cleanup completed");
        } catch (Exception e) {
            logger.error("❌ Error during emergency token cleanup: {}", e.getMessage());
        }
    }
}