package com.ra.freshChickenAPI.service;

import com.ra.freshChickenAPI.entity.Admin;
import com.ra.freshChickenAPI.entity.Customer;
import com.ra.freshChickenAPI.entity.FCMToken;
import com.ra.freshChickenAPI.repository.AdminRepository;
import com.ra.freshChickenAPI.repository.CustomerRepository;
import com.ra.freshChickenAPI.repository.FCMTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FCMTokenService {

    private static final Logger logger = LoggerFactory.getLogger(FCMTokenService.class);

    @Autowired
    private FCMTokenRepository fcmTokenRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Register or update FCM token for an admin
     */
    @Transactional
    public void registerToken(Long adminId, String token, String deviceInfo) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        // Check if token already exists
        Optional<FCMToken> existingToken = fcmTokenRepository.findByToken(token);

        if (existingToken.isPresent()) {
            // Update existing token
            FCMToken fcmToken = existingToken.get();
            fcmToken.setIsActive(true);
            fcmToken.setDeviceInfo(deviceInfo);
            fcmToken.markAsUsed();
            fcmTokenRepository.save(fcmToken);
        } else {
            // Deactivate old tokens for this admin
            fcmTokenRepository.deactivateOldTokensForAdmin(admin, token);

            // Create new token
            FCMToken newToken = new FCMToken();
            newToken.setAdmin(admin);
            newToken.setToken(token);
            newToken.setDeviceInfo(deviceInfo);
            newToken.setIsActive(true);
            fcmTokenRepository.save(newToken);
        }
    }

    /**
     * Get all active FCM tokens for an admin
     */
    public List<String> getActiveTokensForAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        return fcmTokenRepository.findByAdminAndIsActiveTrue(admin)
                .stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());
    }

    /**
     * Get all active FCM tokens for all admins (for broadcasting)
     */
    public List<String> getAllActiveTokens() {
        return fcmTokenRepository.findAllActiveTokens()
                .stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate a token
     */
    @Transactional
    public void deactivateToken(String token) {
        Optional<FCMToken> fcmToken = fcmTokenRepository.findByToken(token);
        if (fcmToken.isPresent()) {
            fcmToken.get().setIsActive(false);
            fcmTokenRepository.save(fcmToken.get());
        }
    }

    /**
     * Check if token is active
     */
    public boolean isTokenActive(String token) {
        return fcmTokenRepository.existsByTokenAndIsActiveTrue(token);
    }

    /**
     * Clean up old inactive tokens (older than specified days)
     */
    @Transactional
    public void cleanupOldTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        fcmTokenRepository.deleteInactiveTokensOlderThan(cutoffDate);
    }

    /**
     * Get token count for admin
     */
    public long getTokenCountForAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        return fcmTokenRepository.findByAdminAndIsActiveTrue(admin).size();
    }

    /**
     * Register or update FCM token for a customer
     */
    @Transactional
    public void registerCustomerToken(Long customerId, String token, String deviceInfo) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Check if token already exists
        Optional<FCMToken> existingToken = fcmTokenRepository.findByToken(token);
        
        Admin admin = adminRepository.findAll().get(0);

        if (existingToken.isPresent()) {
            // Update existing token
            FCMToken fcmToken = existingToken.get();
            fcmToken.setCustomer(customer);
            fcmToken.setAdmin(admin);
            fcmToken.setIsActive(true);
            fcmToken.setDeviceInfo(deviceInfo);
            fcmToken.markAsUsed();
            fcmTokenRepository.save(fcmToken);
        } else {
            // Deactivate old tokens for this customer
            fcmTokenRepository.deactivateOldTokensForCustomer(customer, token);

            // Create new token
            FCMToken newToken = new FCMToken();
            newToken.setCustomer(customer);
            newToken.setAdmin(admin);
            newToken.setToken(token);
            newToken.setDeviceInfo(deviceInfo);
            newToken.setIsActive(true);
            fcmTokenRepository.save(newToken);
        }
    }

    /**
     * Get all active FCM tokens for a customer
     */
    public List<String> getActiveTokensForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        return fcmTokenRepository.findByCustomerAndIsActiveTrue(customer)
                .stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());
    }

    /**
     * Unregister FCM token for a customer
     */
    @Transactional
    public void unregisterCustomerToken(Long customerId, String token) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Optional<FCMToken> fcmToken = fcmTokenRepository.findByToken(token);
        if (fcmToken.isPresent() && fcmToken.get().getCustomer() != null &&
            fcmToken.get().getCustomer().getId().equals(customerId)) {
            fcmToken.get().setIsActive(false);
            fcmTokenRepository.save(fcmToken.get());
        }
    }

    /**
     * Get token count for customer
     */
    public long getTokenCountForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        return fcmTokenRepository.findByCustomerAndIsActiveTrue(customer).size();
    }

    /**
     * Validate and cleanup invalid tokens
     * This is a placeholder method - actual validation would require FCM API calls
     */
    public void validateAndCleanupTokens() {
        // Get all active tokens that haven't been used in the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        List<FCMToken> staleTokens = fcmTokenRepository.findStaleTokens(thirtyDaysAgo);
        
        for (FCMToken token : staleTokens) {
            logger.info("🗑️ Deactivating stale token: {}", token.getToken());
            token.setIsActive(false);
            fcmTokenRepository.save(token);
        }
        
        if (!staleTokens.isEmpty()) {
            logger.info("✅ Cleaned up {} stale FCM tokens", staleTokens.size());
        }
    }
}