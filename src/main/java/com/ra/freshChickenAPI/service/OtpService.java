package com.ra.freshChickenAPI.service;

import com.ra.freshChickenAPI.entity.OtpVerification;
import com.ra.freshChickenAPI.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    
    private final OtpVerificationRepository otpRepository;
    private final SmsService smsService;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    @Transactional
    public boolean generateAndSendOtp(String phoneNumber) {
        try {
            // Delete any existing OTP for this phone number
            otpRepository.deleteByPhoneNumber(phoneNumber);
            
            // Generate random 6-digit OTP
            String otp = generateOtp();
            
            // Create OTP verification record
            OtpVerification otpVerification = new OtpVerification();
            otpVerification.setPhoneNumber(phoneNumber);
            otpVerification.setOtp(otp);
            otpVerification.setCreatedAt(LocalDateTime.now());
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            otpVerification.setIsVerified(false);
            otpVerification.setAttempts(0);
            
            otpRepository.save(otpVerification);
            
            // Send OTP via SMS
            boolean smsSent = smsService.sendOtp(phoneNumber, otp);
            
            if (!smsSent) {
                log.error("Failed to send OTP SMS to {}", phoneNumber);
                return false;
            }
            
            log.info("OTP generated and sent successfully to {}", phoneNumber);
            return true;
            
        } catch (Exception e) {
            log.error("Error generating OTP for {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public boolean verifyOtp(String phoneNumber, String otp) {
        try {
            // Find active OTP
            Optional<OtpVerification> otpOptional = otpRepository
                .findByPhoneNumberAndIsVerifiedFalseAndExpiresAtAfter(
                    phoneNumber, LocalDateTime.now());
            
            if (otpOptional.isEmpty()) {
                log.warn("No valid OTP found for {}", phoneNumber);
                return false;
            }
            
            OtpVerification otpVerification = otpOptional.get();
            
            // Check if max attempts exceeded
            if (otpVerification.getAttempts() >= MAX_ATTEMPTS) {
                log.warn("Max OTP attempts exceeded for {}", phoneNumber);
                otpRepository.delete(otpVerification);
                return false;
            }
            
            // Increment attempts
            otpVerification.setAttempts(otpVerification.getAttempts() + 1);
            otpRepository.save(otpVerification);
            
            // Verify OTP
            if (otpVerification.getOtp().equals(otp)) {
                otpVerification.setIsVerified(true);
                otpRepository.save(otpVerification);
                log.info("OTP verified successfully for {}", phoneNumber);
                return true;
            }
            
            log.warn("Invalid OTP for {}", phoneNumber);
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying OTP for {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
    }
}