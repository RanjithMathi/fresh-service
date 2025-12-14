package com.ra.freshChickenAPI.controller;

import com.ra.freshChickenAPI.service.FCMTokenService;
import com.ra.freshChickenAPI.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/customer/notifications")
@CrossOrigin(origins = "*")
public class CustomerNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerNotificationController.class);

    @Autowired
    private FCMTokenService fcmTokenService;

    @Autowired
    private CustomerService customerService;

    /**
     * Register or update FCM token for a customer
     */
    @PostMapping("/register-token")
    public ResponseEntity<Void> registerCustomerToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CustomerFCMTokenRequest request) {
        try {
            // Get customer ID from request body (sent by mobile app) or JWT token
            Long customerId = request.getCustomerId();
            
            // If customerId not in request body, try to extract from token
            if (customerId == null) {
                if (authHeader != null) {
                    customerId = getCurrentCustomerId(authHeader);
                } else {
                    logger.error("❌ No customer ID found in request body and no auth token provided");
                    return ResponseEntity.badRequest().build();
                }
            }
            
            if (customerId == null) {
                logger.error("❌ No customer ID found in request or token");
                return ResponseEntity.badRequest().build();
            }

            logger.info("📝 Registering FCM token for customer ID: {}", customerId);

            // Verify customer exists
            if (!customerService.getCustomerById(customerId).isPresent()) {
                logger.error("❌ Customer not found with ID: {}", customerId);
                return ResponseEntity.notFound().build();
            }

            // Validate request fields
            if (request.getFcmToken() == null || request.getFcmToken().trim().isEmpty()) {
                logger.error("❌ FCM token is missing or empty");
                return ResponseEntity.badRequest().build();
            }

            fcmTokenService.registerCustomerToken(
                customerId,
                request.getFcmToken(),
                request.getDeviceInfo()
            );
            
            logger.info("✅ FCM token registered successfully for customer: {}", customerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error registering FCM token: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Unregister FCM token for a customer
     */
    @DeleteMapping("/unregister-token")
    public ResponseEntity<Void> unregisterCustomerToken(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UnregisterTokenRequest request) {
        try {
            // Get customer ID from request body or JWT token
            Long customerId = request.getCustomerId();
            
            if (customerId == null) {
                customerId = getCurrentCustomerId(authHeader);
            }
            
            if (customerId == null) {
                logger.error("❌ No customer ID found for token unregistration");
                return ResponseEntity.badRequest().build();
            }

            logger.info("🗑️ Unregistering FCM token for customer ID: {}", customerId);
            fcmTokenService.unregisterCustomerToken(customerId, request.getFcmToken());
            
            logger.info("✅ FCM token unregistered successfully for customer: {}", customerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error unregistering FCM token: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get customer's active FCM tokens
     */
    @GetMapping("/tokens")
    public ResponseEntity<Map<String, Object>> getCustomerTokens(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long customerId = getCurrentCustomerId(authHeader);
            
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            var tokens = fcmTokenService.getActiveTokensForCustomer(customerId);
            long tokenCount = fcmTokenService.getTokenCountForCustomer(customerId);
            
            return ResponseEntity.ok(Map.of(
                "tokens", tokens,
                "tokenCount", tokenCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check if notifications are enabled for customer
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNotificationStatus(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long customerId = getCurrentCustomerId(authHeader);
            
            if (customerId == null) {
                return ResponseEntity.badRequest().build();
            }

            long tokenCount = fcmTokenService.getTokenCountForCustomer(customerId);
            boolean notificationsEnabled = tokenCount > 0;
            
            return ResponseEntity.ok(Map.of(
                "notificationsEnabled", notificationsEnabled,
                "tokenCount", tokenCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Test endpoint to verify backend connectivity
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        logger.info("🧪 Test endpoint called - backend is accessible");
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Backend connection test successful",
            "timestamp", java.time.Instant.now().toString(),
            "service", "Customer Notification Service"
        ));
    }

    /**
     * Test FCM token registration (for debugging)
     */
    @PostMapping("/test-register")
    public ResponseEntity<Map<String, Object>> testTokenRegistration(
            @RequestBody TestRegistrationRequest request) {
        try {
            logger.info("🧪 Test FCM registration called");
            logger.info("Customer ID: {}", request.getCustomerId());
            logger.info("FCM Token: {}", request.getFcmToken() != null ? "present" : "null");
            logger.info("Device Info: {}", request.getDeviceInfo());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test registration successful",
                "receivedCustomerId", request.getCustomerId(),
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Test registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private Long getCurrentCustomerId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            try {
                // First, try to extract from JWT format (customer_token_{id})
                if (token.startsWith("customer_token_")) {
                    String customerIdStr = token.substring("customer_token_".length());
                    return Long.parseLong(customerIdStr);
                }
                
                // For simple tokens (from mobile app), try to get customer ID from database
                // This handles the case where the token is a simple auth token
                // In this case, we would need to look up the customer by token
                // For now, return null to indicate we need a different approach
                
                logger.warn("🔍 Unknown token format: {}", token.substring(0, Math.min(20, token.length())) + "...");
                return null;
                
            } catch (NumberFormatException e) {
                logger.error("❌ Error parsing customer ID from token: {}", e.getMessage());
                return null;
            } catch (Exception e) {
                logger.error("❌ Unexpected error in getCurrentCustomerId: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    // DTOs
    public static class CustomerFCMTokenRequest {
        private String fcmToken;
        private String deviceInfo;
        private Long customerId; // Explicit customer ID from mobile app
        private String timestamp;

        public String getFcmToken() { 
            return fcmToken; 
        }
        
        public void setFcmToken(String fcmToken) { 
            this.fcmToken = fcmToken; 
        }
        
        public String getDeviceInfo() { 
            return deviceInfo; 
        }
        
        public void setDeviceInfo(String deviceInfo) { 
            this.deviceInfo = deviceInfo; 
        }
        
        public Long getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class UnregisterTokenRequest {
        private String fcmToken;
        private Long customerId;

        public String getFcmToken() { 
            return fcmToken; 
        }
        
        public void setFcmToken(String fcmToken) { 
            this.fcmToken = fcmToken; 
        }
        
        public Long getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
    }

    public static class TestRegistrationRequest {
        private Long customerId;
        private String fcmToken;
        private String deviceInfo;

        public Long getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
        
        public String getFcmToken() {
            return fcmToken;
        }
        
        public void setFcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
        }
        
        public String getDeviceInfo() {
            return deviceInfo;
        }
        
        public void setDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
        }
    }
}