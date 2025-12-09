package com.willbank.notificationservice.repository;

import com.willbank.notificationservice.domain.Notification;
import com.willbank.notificationservice.domain.NotificationStatus;
import com.willbank.notificationservice.domain.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for Notification entities
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Find all notifications for a specific customer
     */
    List<Notification> findByCustomerId(Long customerId);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find notifications by type
     */
    List<Notification> findByType(NotificationType type);

    /**
     * Find notifications by email
     */
    List<Notification> findByEmail(String email);

    /**
     * Find notifications created after a specific date
     */
    List<Notification> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find failed notifications for retry
     */
    List<Notification> findByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime date);
}
