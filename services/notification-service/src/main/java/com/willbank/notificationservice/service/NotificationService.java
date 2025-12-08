package com.willbank.notificationservice.service;

import com.willbank.notificationservice.domain.Notification;
import com.willbank.notificationservice.domain.NotificationStatus;
import com.willbank.notificationservice.domain.NotificationType;
import com.willbank.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing notification records and history
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Get all notifications for a customer
     */
    public List<Notification> getCustomerNotifications(Long customerId) {
        log.debug("Fetching notifications for customer: {}", customerId);
        return notificationRepository.findByCustomerId(customerId);
    }

    /**
     * Get notifications by status
     */
    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        log.debug("Fetching notifications with status: {}", status);
        return notificationRepository.findByStatus(status);
    }

    /**
     * Get notifications by type
     */
    public List<Notification> getNotificationsByType(NotificationType type) {
        log.debug("Fetching notifications of type: {}", type);
        return notificationRepository.findByType(type);
    }

    /**
     * Get notification by ID
     */
    public Notification getNotificationById(String id) {
        log.debug("Fetching notification by ID: {}", id);
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    /**
     * Get recent notifications (last 30 days)
     */
    public List<Notification> getRecentNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        log.debug("Fetching notifications created after: {}", thirtyDaysAgo);
        return notificationRepository.findByCreatedAtAfter(thirtyDaysAgo);
    }

    /**
     * Get failed notifications for potential retry
     */
    public List<Notification> getFailedNotifications(int hoursAgo) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        log.debug("Fetching failed notifications since: {}", since);
        return notificationRepository.findByStatusAndCreatedAtAfter(
                NotificationStatus.FAILED, since);
    }

    /**
     * Get all notifications
     */
    public List<Notification> getAllNotifications() {
        log.debug("Fetching all notifications");
        return notificationRepository.findAll();
    }

    /**
     * Delete old notifications (cleanup)
     */
    public void deleteOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        log.info("Deleting notifications older than: {}", cutoffDate);

        List<Notification> oldNotifications = notificationRepository.findByCreatedAtAfter(cutoffDate);
        notificationRepository.deleteAll(oldNotifications);

        log.info("Deleted {} old notifications", oldNotifications.size());
    }
}
