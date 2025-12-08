package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserId(String userId, Pageable pageable);
    
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    List<Notification> findByStatusAndRetryCountLessThan(
        Notification.NotificationStatus status, 
        Integer maxRetries
    );
    
    Page<Notification> findByCreatedAtBetween(
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    Long countByStatus(Notification.NotificationStatus status);
}
