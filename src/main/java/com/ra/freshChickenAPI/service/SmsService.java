package com.ra.freshChickenAPI.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public boolean sendOtp(String phoneNumber, String otp) {
        try {
            // Initialize Twilio
            Twilio.init(accountSid, authToken);

            // Format phone number to international format (assuming Indian numbers)
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);

            // Create SMS message
            String messageBody = String.format(
                "Your Fresh Chicken verification code is: %s. Valid for 5 minutes. Do not share with anyone.",
                otp
            );

            Message message = Message.creator(
                new PhoneNumber(formattedPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                messageBody
            ).create();

            log.info("OTP SMS sent successfully to {} via Twilio. Message SID: {}", phoneNumber, message.getSid());

            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", phoneNumber, e.getMessage());

            // Fallback to logging for development/testing if Twilio fails
            if (isDevelopmentMode()) {
                log.warn("Falling back to logging OTP for development");
                log.info("=".repeat(50));
                log.info("SMS: Your Fresh Chicken verification code is: {}", otp);
                log.info("Valid for 5 minutes. Do not share with anyone.");
                log.info("=".repeat(50));
                return true;
            }

            return false;
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove any existing +91 prefix if present
        if (phoneNumber.startsWith("+91")) {
            phoneNumber = phoneNumber.substring(3);
        }

        // Remove any spaces, dashes, or other non-digit characters
        phoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        // Add +91 prefix for Indian numbers
        return "+91" + phoneNumber;
    }

    private boolean isDevelopmentMode() {
        // Check if Twilio credentials are placeholder values
        return "your_account_sid_here".equals(accountSid) ||
               "your_auth_token_here".equals(authToken) ||
               "+13526463596".equals(fromPhoneNumber);
    }
}