package com.example.notificationservice.service;

import com.example.notificationservice.entity.Notification;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {
    
    public void sendPushNotification(Notification notification, String fcmToken) throws FirebaseMessagingException {
        if (fcmToken == null || fcmToken.isEmpty()) {
            throw new IllegalArgumentException("FCM token is required for push notifications");
        }
        
        try {
            // Check if Firebase is initialized
            if (FirebaseMessaging.getInstance() == null) {
                log.warn("Firebase not initialized - skipping push notification");
                return;
            }
            
            Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                    .setTitle(notification.getSubject())
                    .setBody(notification.getContent())
                    .build())
                .putData("notificationId", notification.getId().toString())
                .putData("type", notification.getType().name())
                .putData("userId", notification.getUserId() != null ? notification.getUserId() : "")
                .putData("transactionId", notification.getTransactionId() != null ? notification.getTransactionId() : "")
                .build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent successfully. Response: {}", response);
            
        } catch (IllegalStateException e) {
            log.warn("Firebase not initialized - skipping push notification: {}", e.getMessage());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification: {}", e.getMessage());
            throw e;
        }
    }
    
    public void sendPushNotificationWithData(String fcmToken, String title, String body, Map<String, String> data) 
            throws FirebaseMessagingException {
        
        try {
            if (FirebaseMessaging.getInstance() == null) {
                log.warn("Firebase not initialized - skipping push notification");
                return;
            }
            
            Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());
            
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Push notification with data sent successfully. Response: {}", response);
        } catch (IllegalStateException e) {
            log.warn("Firebase not initialized - skipping push notification");
        }
    }
    
    public void sendMulticastNotification(String title, String body, java.util.List<String> tokens) 
            throws FirebaseMessagingException {
        
        try {
            if (FirebaseMessaging.getInstance() == null) {
                log.warn("Firebase not initialized - skipping multicast notification");
                return;
            }
            
            MulticastMessage message = MulticastMessage.builder()
                .setNotification(com.google.firebase.messaging.Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .addAllTokens(tokens)
                .build();
            
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Multicast notification sent. Success: {}, Failure: {}", 
                response.getSuccessCount(), response.getFailureCount());
        } catch (IllegalStateException e) {
            log.warn("Firebase not initialized - skipping multicast notification");
        }
    }
}
