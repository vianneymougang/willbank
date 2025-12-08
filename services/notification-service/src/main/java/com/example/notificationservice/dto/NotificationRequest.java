package com.example.notificationservice.dto;

import com.example.notificationservice.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Type is required")
    private Notification.NotificationType type;
    
    private String userId;
    private String transactionId;
    private Long templateId;
    private String fcmToken;
    private Map<String, Object> templateData;
}
