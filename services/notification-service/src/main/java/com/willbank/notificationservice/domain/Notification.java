package com.willbank.notificationservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Notification entity stored in MongoDB
 * Tracks all notifications sent to customers
 */
@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    @Indexed
    private Long customerId;

    @Indexed
    private String email;

    @Indexed
    private NotificationType type;

    private String subject;

    private String content;

    @Indexed
    private NotificationStatus status;

    private String errorMessage;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    // Metadata
    private String relatedEntityId; // e.g., accountId, transactionId
    private String relatedEntityType; // e.g., "ACCOUNT", "TRANSACTION"
}
