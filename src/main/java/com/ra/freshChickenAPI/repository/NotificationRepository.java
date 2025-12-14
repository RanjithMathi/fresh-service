package com.ra.freshChickenAPI.repository;

import com.ra.freshChickenAPI.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false")
    long countByIsReadFalse();

    List<Notification> findByCreatedAtAfter(LocalDateTime since);
}