package com.example.notificationservice.dto;

import com.example.notificationservice.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private String recipient;
    private String subject;
    private String content;
    private Notification.NotificationType type;
    private Notification.NotificationStatus status;
    private String userId;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String errorMessage;
    
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .recipient(notification.getRecipient())
            .subject(notification.getSubject())
            .content(notification.getContent())
            .type(notification.getType())
            .status(notification.getStatus())
            .userId(notification.getUserId())
            .transactionId(notification.getTransactionId())
            .createdAt(notification.getCreatedAt())
            .sentAt(notification.getSentAt())
            .errorMessage(notification.getErrorMessage())
            .build();
    }
}
