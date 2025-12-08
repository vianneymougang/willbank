package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.entity.NotificationTemplate;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    
    @Transactional
    public NotificationResponse createAndSendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
            .recipient(request.getRecipient())
            .subject(request.getSubject())
            .content(request.getContent())
            .type(request.getType())
            .status(Notification.NotificationStatus.PENDING)
            .userId(request.getUserId())
            .transactionId(request.getTransactionId())
            .templateId(request.getTemplateId())
            .build();
        
        notification = notificationRepository.save(notification);
        
        try {
            sendNotification(notification, request.getFcmToken());
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }
        
        notification = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(notification);
    }
    
    private void sendNotification(Notification notification, String fcmToken) throws Exception {
        switch (notification.getType()) {
            case EMAIL:
                emailService.sendEmail(notification);
                break;
            case PUSH:
                pushNotificationService.sendPushNotification(notification, fcmToken);
                break;
            case BOTH:
                emailService.sendEmail(notification);
                pushNotificationService.sendPushNotification(notification, fcmToken);
                break;
        }
    }
    
    @Transactional
    public NotificationResponse createNotificationFromTemplate(String templateName, 
                                                               String recipient, 
                                                               Map<String, Object> data,
                                                               String fcmToken) {
        NotificationTemplate template = templateRepository.findByNameAndActiveTrue(templateName)
            .orElseThrow(() -> new RuntimeException("Template not found: " + templateName));
        
        String content = replaceTemplateVariables(template.getEmailTemplate(), data);
        String subject = replaceTemplateVariables(template.getSubject(), data);
        
        NotificationRequest request = NotificationRequest.builder()
            .recipient(recipient)
            .subject(subject)
            .content(content)
            .type(template.getType())
            .templateId(template.getId())
            .fcmToken(fcmToken)
            .build();
        
        return createAndSendNotification(request);
    }
    
    private String replaceTemplateVariables(String template, Map<String, Object> data) {
        String result = template;
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
            }
        }
        return result;
    }
    
    public Page<NotificationResponse> getNotificationHistory(Pageable pageable) {
        return notificationRepository.findAll(pageable)
            .map(NotificationResponse::fromEntity);
    }
    
    public Page<NotificationResponse> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
            .map(NotificationResponse::fromEntity);
    }
    
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
        return NotificationResponse.fromEntity(notification);
    }
    
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository
            .findByStatusAndRetryCountLessThan(Notification.NotificationStatus.FAILED, 3);
        
        for (Notification notification : failedNotifications) {
            try {
                sendNotification(notification, null);
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setErrorMessage(e.getMessage());
            }
            notificationRepository.save(notification);
        }
    }
    
    public Map<String, Long> getNotificationStats() {
        return Map.of(
            "total", notificationRepository.count(),
            "sent", notificationRepository.countByStatus(Notification.NotificationStatus.SENT),
            "failed", notificationRepository.countByStatus(Notification.NotificationStatus.FAILED),
            "pending", notificationRepository.countByStatus(Notification.NotificationStatus.PENDING)
        );
    }
}
