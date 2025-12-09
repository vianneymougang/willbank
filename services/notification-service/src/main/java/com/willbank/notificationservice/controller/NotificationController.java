package com.willbank.notificationservice.controller;

import com.willbank.notificationservice.domain.Notification;
import com.willbank.notificationservice.domain.NotificationStatus;
import com.willbank.notificationservice.domain.NotificationType;
import com.willbank.notificationservice.dto.NotificationResponse;
import com.willbank.notificationservice.mapper.NotificationMapper;
import com.willbank.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for notification management
 * Provides endpoints to query notification history
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for querying notification history")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for a customer
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer notifications", description = "Retrieve all notifications sent to a specific customer")
    public ResponseEntity<List<NotificationResponse>> getCustomerNotifications(
            @PathVariable Long customerId) {
        log.info("REST request to get notifications for customer: {}", customerId);

        List<Notification> notifications = notificationService.getCustomerNotifications(customerId);
        List<NotificationResponse> response = NotificationMapper.toResponseList(notifications);

        return ResponseEntity.ok(response);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id) {
        log.info("REST request to get notification by ID: {}", id);

        Notification notification = notificationService.getNotificationById(id);
        NotificationResponse response = NotificationMapper.toResponse(notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all notifications
     */
    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieve all notifications in the system")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        log.info("REST request to get all notifications");

        List<Notification> notifications = notificationService.getAllNotifications();
        List<NotificationResponse> response = NotificationMapper.toResponseList(notifications);

        return ResponseEntity.ok(response);
    }

    /**
     * Get notifications by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get notifications by status", description = "Retrieve notifications filtered by status (PENDING, SENT, FAILED)")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByStatus(
            @PathVariable NotificationStatus status) {
        log.info("REST request to get notifications with status: {}", status);

        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        List<NotificationResponse> response = NotificationMapper.toResponseList(notifications);

        return ResponseEntity.ok(response);
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type", description = "Retrieve notifications filtered by type")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @PathVariable NotificationType type) {
        log.info("REST request to get notifications of type: {}", type);

        List<Notification> notifications = notificationService.getNotificationsByType(type);
        List<NotificationResponse> response = NotificationMapper.toResponseList(notifications);

        return ResponseEntity.ok(response);
    }

    /**
     * Get recent notifications (last 30 days)
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent notifications", description = "Retrieve notifications from the last 30 days")
    public ResponseEntity<List<NotificationResponse>> getRecentNotifications() {
        log.info("REST request to get recent notifications");

        List<Notification> notifications = notificationService.getRecentNotifications();
        List<NotificationResponse> response = NotificationMapper.toResponseList(notifications);

        return ResponseEntity.ok(response);
    }

    /**
     * Get failed notifications for retry
     */
    @GetMapping("/failed")
    @Operation(summary = "Get failed notifications", description = "Retrieve notifications that failed to send")
    public ResponseEntity<List<NotificationResponse>> getFailedNotifications(
            @RequestParam(defaultValue = "24") int hoursAgo) {
        log.info("REST request to get failed notifications from last {} hours", hoursAgo);

        List<Notification> notifications = notificationService.getFailedNotifications(hoursAgo);
        List<NotificationResponse> response = NotificationMapper.toResponseList(notifications);

        return ResponseEntity.ok(response);
    }
}
