package com.example.notificationservice.service;

import com.example.notificationservice.entity.Notification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {
    
    @InjectMocks
    private PushNotificationService pushNotificationService;
    
    @Test
    void testSendPushNotification_NullToken_ThrowsException() {
        Notification notification = Notification.builder()
            .subject("Test")
            .content("Test Content")
            .type(Notification.NotificationType.PUSH)
            .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            pushNotificationService.sendPushNotification(notification, null);
        });
    }
}
