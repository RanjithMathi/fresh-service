package com.ra.freshChickenAPI.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.MessagingErrorCode;
import com.ra.freshChickenAPI.repository.FCMTokenRepository;
import com.ra.freshChickenAPI.entity.FCMToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class FirebaseMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);

    private final FirebaseMessaging firebaseMessaging;

    @Autowired
    private FCMTokenRepository fcmTokenRepository;

    public FirebaseMessagingService() {
        this.firebaseMessaging = FirebaseMessaging.getInstance();
        logger.info("✅ Firebase Messaging Service initialized");
    }

    /**
     * Send FCM notification to a single device
     */
    public void sendNotificationToToken(String token, String title, String body, String type, String data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putData("type", type)
                    .putData("message", data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setChannelId("order_notifications")
                                    .build())
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            logger.info("✅ Successfully sent FCM message: {}", response);
        } catch (FirebaseMessagingException e) {
            logger.error("❌ Failed to send FCM message to token {}: {}", token, e.getMessage());
            // Log error code if available
            if (e.getMessagingErrorCode() != null) {
                logger.error("Error code: {}", e.getMessagingErrorCode());
            }
        }
    }

    /**
     * Send FCM notification to multiple devices
     */
    public void sendNotificationToTokens(List<String> tokens, String title, String body, String type, String data) {
        if (tokens == null || tokens.isEmpty()) {
            logger.warn("⚠️ No FCM tokens provided for notification");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Create multicast message
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .putData("type", type)
                    .putData("message", data)
                    .addAllTokens(tokens)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setChannelId("order_notifications")
                                    .build())
                            .build())
                    .build();

            // ✅ FIXED: Use sendEachForMulticast instead of sendMulticast
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            
            logger.info("✅ Successfully sent FCM multicast message. Success count: {}, Failure count: {}",
                       response.getSuccessCount(), response.getFailureCount());

            // Log failures with details and handle invalid tokens
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String failedToken = tokens.get(i);
                        FirebaseMessagingException exception = responses.get(i).getException();
                        
                        logger.error("❌ FCM send failed for token {}: {}", 
                                failedToken, exception.getMessage());
                        
                        // Handle specific FCM errors
                        if (isInvalidTokenError(exception)) {
                            logger.warn("🗑️ Removing invalid token: {}", failedToken);
                            removeInvalidToken(failedToken);
                        } else if (isQuotaExceededError(exception)) {
                            logger.warn("⚠️ Quota exceeded for token: {}", failedToken);
                            // Could implement retry logic here
                        } else {
                            logger.error("Error code: {}", exception.getMessagingErrorCode());
                        }
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            logger.error("❌ Failed to send FCM multicast message: {}", e.getMessage());
            if (e.getMessagingErrorCode() != null) {
                logger.error("Error code: {}", e.getMessagingErrorCode());
            }
        }
    }

    /**
     * Send order created notification to single token
     */
    public void sendOrderCreatedNotification(String token, Long orderId, String customerName, String totalAmount) {
        String title = "New Order Received";
        String body = "New order #" + orderId + " from " + customerName;
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\", \"totalAmount\": \"%s\"}",
                                   orderId, customerName, totalAmount);

        sendNotificationToToken(token, title, body, "ORDER_CREATED", data);
    }

    /**
     * Send order created notification to multiple tokens
     */
    public void sendOrderCreatedNotification(List<String> tokens, Long orderId, String customerName, String totalAmount) {
        String title = "New Order Received";
        String body = "New order #" + orderId + " from " + customerName;
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\", \"totalAmount\": \"%s\"}",
                                   orderId, customerName, totalAmount);

        sendNotificationToTokens(tokens, title, body, "ORDER_CREATED", data);
    }

    /**
     * Send order status updated notification to single token
     */
    public void sendOrderStatusUpdatedNotification(String token, Long orderId, String customerName,
                                                  String oldStatus, String newStatus) {
        String title = "Order Status Updated";
        String body = "Order #" + orderId + " status changed to " + newStatus;
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\", \"oldStatus\": \"%s\", \"newStatus\": \"%s\"}",
                                   orderId, customerName, oldStatus, newStatus);

        sendNotificationToToken(token, title, body, "ORDER_STATUS_UPDATED", data);
    }

    /**
     * Send order status updated notification to multiple tokens
     */
    public void sendOrderStatusUpdatedNotification(List<String> tokens, Long orderId, String customerName,
                                                  String oldStatus, String newStatus) {
        String title = "Order Status Updated";
        String body = "Order #" + orderId + " status changed to " + newStatus;
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\", \"oldStatus\": \"%s\", \"newStatus\": \"%s\"}",
                                   orderId, customerName, oldStatus, newStatus);

        sendNotificationToTokens(tokens, title, body, "ORDER_STATUS_UPDATED", data);
    }

    /**
     * Send order cancelled notification
     */
    public void sendOrderCancelledNotification(String token, Long orderId, String customerName) {
        String title = "Order Cancelled";
        String body = "Order #" + orderId + " has been cancelled";
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\"}", orderId, customerName);

        sendNotificationToToken(token, title, body, "ORDER_CANCELLED", data);
    }

    /**
     * Send payment received notification
     */
    public void sendPaymentReceivedNotification(String token, Long orderId, String customerName, String amount) {
        String title = "Payment Received";
        String body = "Payment of ₹" + amount + " received for order #" + orderId;
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\", \"amount\": \"%s\"}",
                                   orderId, customerName, amount);

        sendNotificationToToken(token, title, body, "PAYMENT_RECEIVED", data);
    }

    /**
     * Send delivery started notification
     */
    public void sendDeliveryStartedNotification(String token, Long orderId, String customerName) {
        String title = "Delivery Started";
        String body = "Your order #" + orderId + " is out for delivery";
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\"}", orderId, customerName);

        sendNotificationToToken(token, title, body, "DELIVERY_STARTED", data);
    }

    /**
     * Send delivery completed notification
     */
    public void sendDeliveryCompletedNotification(String token, Long orderId, String customerName) {
        String title = "Order Delivered";
        String body = "Order #" + orderId + " has been delivered successfully";
        String data = String.format("{\"orderId\": %d, \"customerName\": \"%s\"}", orderId, customerName);

        sendNotificationToToken(token, title, body, "DELIVERY_COMPLETED", data);
    }

    /**
     * Send customer order status notification
     */
    public void sendCustomerOrderStatusNotification(List<String> tokens, Long orderId, String oldStatus, String newStatus) {
        String title = getCustomerNotificationTitle(newStatus);
        String body = getCustomerNotificationBody(orderId, oldStatus, newStatus);
        String data = String.format("{\"orderId\": %d, \"oldStatus\": \"%s\", \"newStatus\": \"%s\"}",
                                   orderId, oldStatus, newStatus);

        sendNotificationToTokens(tokens, title, body, "ORDER_STATUS_UPDATE", data);
    }

    /**
     * Send customer order confirmation notification
     */
    public void sendCustomerOrderConfirmationNotification(List<String> tokens, Long orderId, String totalAmount) {
        String title = "Order Confirmed";
        String body = "Your order #" + orderId + " has been confirmed and is being prepared";
        String data = String.format("{\"orderId\": %d, \"totalAmount\": \"%s\"}", orderId, totalAmount);

        sendNotificationToTokens(tokens, title, body, "ORDER_CONFIRMED", data);
    }

    /**
     * Get customer notification title based on status
     */
    private String getCustomerNotificationTitle(String status) {
        switch (status.toUpperCase()) {
            case "CONFIRMED":
                return "Order Confirmed";
            case "PREPARING":
                return "Order Being Prepared";
            case "OUT_FOR_DELIVERY":
                return "Out for Delivery";
            case "DELIVERED":
                return "Order Delivered";
            case "CANCELLED":
                return "Order Cancelled";
            default:
                return "Order Update";
        }
    }

    /**
     * Get customer notification body based on status
     */
    private String getCustomerNotificationBody(Long orderId, String oldStatus, String newStatus) {
        switch (newStatus.toUpperCase()) {
            case "CONFIRMED":
                return "Your order #" + orderId + " has been confirmed and is being prepared";
            case "PREPARING":
                return "Your order #" + orderId + " is now being prepared";
            case "OUT_FOR_DELIVERY":
                return "Your order #" + orderId + " is out for delivery";
            case "DELIVERED":
                return "Your order #" + orderId + " has been delivered successfully";
            case "CANCELLED":
                return "Your order #" + orderId + " has been cancelled";
            default:
                return "Your order #" + orderId + " status has been updated";
        }
    }

    /**
     * Check if the error indicates an invalid token
     */
    private boolean isInvalidTokenError(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        if (errorCode != null) {
            // Compare using equals() method instead of switch case
            if (errorCode.equals(MessagingErrorCode.UNREGISTERED) ||
                errorCode.equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                return true;
            }
        }
        
        String message = e.getMessage();
        return message != null && (
            message.contains("Requested entity was not found") ||
            message.contains("registration token is not a valid FCM registration token") ||
            message.contains("invalid registration token")
        );
    }

    /**
     * Check if the error indicates quota exceeded
     */
    private boolean isQuotaExceededError(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        if (errorCode != null) {
            // Compare using equals() method instead of switch case
            if (errorCode.equals(MessagingErrorCode.QUOTA_EXCEEDED)) {
                return true;
            }
        }
        
        String message = e.getMessage();
        return message != null && (
            message.contains("quota exceeded") ||
            message.contains("rate limit")
        );
    }

    /**
     * Remove invalid token from database
     */
    private void removeInvalidToken(String token) {
        try {
            Optional<FCMToken> fcmToken = fcmTokenRepository.findByToken(token);
            if (fcmToken.isPresent()) {
                FCMToken tokenEntity = fcmToken.get();
                tokenEntity.setIsActive(false);
                fcmTokenRepository.save(tokenEntity);
                logger.info("🗑️ Invalid token deactivated: {}", token);
            }
        } catch (Exception ex) {
            logger.error("❌ Error removing invalid token: {}", ex.getMessage());
        }
    }
}