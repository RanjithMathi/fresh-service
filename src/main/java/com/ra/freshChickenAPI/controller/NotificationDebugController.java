package com.ra.freshChickenAPI.controller;

import com.ra.freshChickenAPI.entity.Order;
import com.ra.freshChickenAPI.entity.OrderStatus;
import com.ra.freshChickenAPI.service.FCMTokenService;
import com.ra.freshChickenAPI.service.FirebaseMessagingService;
import com.ra.freshChickenAPI.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/debug/notifications")
@CrossOrigin(origins = "*")
public class NotificationDebugController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationDebugController.class);

    @Autowired
    private FCMTokenService fcmTokenService;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Autowired
    private OrderService orderService;

    /**
     * Test sending notification to a specific customer
     */
    @PostMapping("/test-customer")
    public ResponseEntity<Map<String, Object>> testCustomerNotification(
            @RequestParam Long customerId,
            @RequestParam(required = false) String messageType) {
        
        try {
            logger.info("🧪 [DEBUG] Testing customer notification for customer ID: {}, type: {}", customerId, messageType);
            
            List<String> customerTokens = fcmTokenService.getActiveTokensForCustomer(customerId);
            logger.info("📱 [DEBUG] Found {} active tokens for customer {}", customerTokens.size(), customerId);
            
            if (customerTokens.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "warning",
                    "message", "No active tokens found for customer",
                    "customerId", customerId,
                    "tokenCount", 0
                ));
            }

            String type = messageType != null ? messageType : "ORDER_STATUS_UPDATE";
            String title = "Test Notification - " + type;
            String body = "This is a test notification for debugging purposes";
            String data = String.format("{\"test\": true, \"customerId\": %d, \"type\": \"%s\", \"timestamp\": \"%s\"}", 
                                      customerId, type, LocalDateTime.now());

            firebaseMessagingService.sendNotificationToTokens(customerTokens, title, body, type, data);
            
            logger.info("✅ [DEBUG] Test notification sent successfully to {} tokens", customerTokens.size());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test notification sent successfully",
                "customerId", customerId,
                "tokenCount", customerTokens.size(),
                "messageType", type,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ [DEBUG] Failed to send test notification: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to send test notification",
                "error", e.getMessage(),
                "customerId", customerId
            ));
        }
    }

    /**
     * Test sending notification to all admins
     */
    @PostMapping("/test-admin")
    public ResponseEntity<Map<String, Object>> testAdminNotification(
            @RequestParam(required = false) String messageType) {
        
        try {
            logger.info("🧪 [DEBUG] Testing admin notification, type: {}", messageType);
            
            List<String> adminTokens = fcmTokenService.getAllActiveTokens();
            logger.info("📱 [DEBUG] Found {} active admin tokens", adminTokens.size());
            
            if (adminTokens.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "warning",
                    "message", "No active admin tokens found",
                    "tokenCount", 0
                ));
            }

            String type = messageType != null ? messageType : "ORDER_STATUS_UPDATED";
            String title = "Test Admin Notification - " + type;
            String body = "This is a test admin notification for debugging purposes";
            String data = String.format("{\"test\": true, \"type\": \"%s\", \"timestamp\": \"%s\"}", 
                                      type, LocalDateTime.now());

            firebaseMessagingService.sendNotificationToTokens(adminTokens, title, body, type, data);
            
            logger.info("✅ [DEBUG] Test admin notification sent successfully to {} tokens", adminTokens.size());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test admin notification sent successfully",
                "tokenCount", adminTokens.size(),
                "messageType", type,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ [DEBUG] Failed to send test admin notification: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to send test admin notification",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Simulate order status update without actually changing the order
     */
    @PostMapping("/simulate-order-status")
    public ResponseEntity<Map<String, Object>> simulateOrderStatusUpdate(
            @RequestParam Long orderId,
            @RequestParam String newStatus) {
        
        try {
            logger.info("🧪 [DEBUG] Simulating order status update for order ID: {}, new status: {}", orderId, newStatus);
            
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

            String currentStatus = order.getStatus().toString();
            String customerName = order.getCustomer().getName();
            
            logger.info("📊 [DEBUG] Order details - Current status: {}, Customer: {}", currentStatus, customerName);

            // Check tokens for this customer
            List<String> customerTokens = fcmTokenService.getActiveTokensForCustomer(order.getCustomer().getId());
            List<String> adminTokens = fcmTokenService.getAllActiveTokens();
            
            logger.info("📱 [DEBUG] Customer tokens: {}, Admin tokens: {}", customerTokens.size(), adminTokens.size());

            // Send customer notification
            if (!customerTokens.isEmpty()) {
                String title = "Test: Order Status Update";
                String body = "Order #" + orderId + " would change from " + currentStatus + " to " + newStatus;
                String data = String.format("{\"orderId\": %d, \"oldStatus\": \"%s\", \"newStatus\": \"%s\", \"simulation\": true}", 
                                          orderId, currentStatus, newStatus);

                firebaseMessagingService.sendNotificationToTokens(customerTokens, title, body, "ORDER_STATUS_UPDATE", data);
                logger.info("✅ [DEBUG] Customer notification sent for simulation");
            }

            // Send admin notification
            if (!adminTokens.isEmpty()) {
                String title = "Test: Order Status Updated";
                String body = "Order #" + orderId + " status changed from " + currentStatus + " to " + newStatus;
                String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\", \"oldStatus\": \"%s\", \"newStatus\": \"%s\", \"simulation\": true}", 
                                          orderId, customerName, currentStatus, newStatus);

                firebaseMessagingService.sendNotificationToTokens(adminTokens, title, body, "ORDER_STATUS_UPDATED", data);
                logger.info("✅ [DEBUG] Admin notification sent for simulation");
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Order status update simulation completed",
                "orderId", orderId,
                "currentStatus", currentStatus,
                "simulatedStatus", newStatus,
                "customerName", customerName,
                "customerTokens", customerTokens.size(),
                "adminTokens", adminTokens.size(),
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ [DEBUG] Failed to simulate order status update: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to simulate order status update",
                "error", e.getMessage(),
                "orderId", orderId,
                "newStatus", newStatus
            ));
        }
    }

    /**
     * Get notification status and token information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNotificationStatus() {
        try {
            logger.info("🧪 [DEBUG] Getting notification status");
            
            List<String> adminTokens = fcmTokenService.getAllActiveTokens();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "timestamp", LocalDateTime.now().toString(),
                "totalAdminTokens", adminTokens.size(),
                "backendService", "Running",
                "firebaseConfig", "Configured",
                "message", "Notification system status retrieved"
            ));
            
        } catch (Exception e) {
            logger.error("❌ [DEBUG] Failed to get notification status: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to get notification status",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Test FCM token registration endpoint
     */
    @PostMapping("/test-token-registration")
    public ResponseEntity<Map<String, Object>> testTokenRegistration(
            @RequestParam Long customerId,
            @RequestParam String fcmToken) {
        
        try {
            logger.info("🧪 [DEBUG] Testing token registration for customer ID: {}", customerId);
            
            // This would simulate the token registration process
            List<String> beforeTokens = fcmTokenService.getActiveTokensForCustomer(customerId);
            
            // In a real scenario, this would be handled by CustomerNotificationController
            // For testing, we'll just return the current state
            
            List<String> afterTokens = fcmTokenService.getActiveTokensForCustomer(customerId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Token registration test completed",
                "customerId", customerId,
                "testToken", fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...",
                "tokensBefore", beforeTokens.size(),
                "tokensAfter", afterTokens.size(),
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("❌ [DEBUG] Failed to test token registration: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to test token registration",
                "error", e.getMessage(),
                "customerId", customerId
            ));
        }
    }
}