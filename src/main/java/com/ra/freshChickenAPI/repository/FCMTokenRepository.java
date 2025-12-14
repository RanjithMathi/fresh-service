package com.ra.freshChickenAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ra.freshChickenAPI.entity.Admin;
import com.ra.freshChickenAPI.entity.Customer;
import com.ra.freshChickenAPI.entity.FCMToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    // Find all active tokens for an admin
    List<FCMToken> findByAdminAndIsActiveTrue(Admin admin);

    // Find token by token string
    Optional<FCMToken> findByToken(String token);

    // Find all active tokens for all admins (for broadcasting notifications)
    @Query("SELECT t FROM FCMToken t WHERE t.isActive = true")
    List<FCMToken> findAllActiveTokens();

    // Deactivate old tokens for an admin (when registering new token)
    @Modifying
    @Query("UPDATE FCMToken t SET t.isActive = false WHERE t.admin = :admin AND t.token != :currentToken")
    void deactivateOldTokensForAdmin(@Param("admin") Admin admin, @Param("currentToken") String currentToken);

    // Check if token exists and is active
    boolean existsByTokenAndIsActiveTrue(String token);

    // Delete inactive tokens (cleanup method)
    @Modifying
    @Query("DELETE FROM FCMToken t WHERE t.isActive = false AND t.updatedAt < :cutoffDate")
    void deleteInactiveTokensOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    // Find all active tokens for a customer
    List<FCMToken> findByCustomerAndIsActiveTrue(Customer customer);

    // Deactivate old tokens for a customer (when registering new token)
    @Modifying
    @Query("UPDATE FCMToken t SET t.isActive = false WHERE t.customer = :customer AND t.token != :currentToken")
    void deactivateOldTokensForCustomer(@Param("customer") Customer customer, @Param("currentToken") String currentToken);

    // Find stale tokens (active tokens not used for a while)
    @Query("SELECT t FROM FCMToken t WHERE t.isActive = true AND (t.lastUsedAt IS NULL OR t.lastUsedAt < :cutoffDate)")
    List<FCMToken> findStaleTokens(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}