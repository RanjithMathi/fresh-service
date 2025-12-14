package com.ra.freshChickenAPI.repository;

import com.ra.freshChickenAPI.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByPhoneNumberAndIsVerifiedFalseAndExpiresAtAfter(
        String phoneNumber, LocalDateTime currentTime);
    
    void deleteByPhoneNumber(String phoneNumber);
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
}
