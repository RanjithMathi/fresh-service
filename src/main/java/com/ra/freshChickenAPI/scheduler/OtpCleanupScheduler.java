package com.ra.freshChickenAPI.scheduler;

import com.ra.freshChickenAPI.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {
    
    private final OtpService otpService;
    
    // Run every 10 minutes
    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredOtps() {
        log.info("Running OTP cleanup scheduler");
        otpService.cleanupExpiredOtps();
    }
}