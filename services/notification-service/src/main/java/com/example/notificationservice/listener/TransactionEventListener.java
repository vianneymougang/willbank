package com.example.notificationservice.listener;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.TransactionEvent;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = "${rabbitmq.queue.transactions}")
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {}", event.getTransactionId());
        
        try {
            processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error processing transaction event {}: {}", event.getTransactionId(), e.getMessage());
        }
    }
    
    private void processTransactionEvent(TransactionEvent event) {
        String subject = buildSubject(event);
        String content = buildContent(event);
        
        Notification.NotificationType type = determineNotificationType(event);
        
        NotificationRequest request = NotificationRequest.builder()
            .recipient(event.getRecipientEmail())
            .subject(subject)
            .content(content)
            .type(type)
            .userId(event.getUserId())
            .transactionId(event.getTransactionId())
            .fcmToken(event.getFcmToken())
            .build();
        
        notificationService.createAndSendNotification(request);
        log.info("Notification created for transaction: {}", event.getTransactionId());
    }
    
    private String buildSubject(TransactionEvent event) {
        return switch (event.getType().toUpperCase()) {
            case "DEPOSIT" -> "Dépôt confirmé - " + event.getAmount() + " " + event.getCurrency();
            case "WITHDRAWAL" -> "Retrait effectué - " + event.getAmount() + " " + event.getCurrency();
            case "TRANSFER" -> "Transfert réalisé - " + event.getAmount() + " " + event.getCurrency();
            default -> "Transaction " + event.getStatus() + " - " + event.getTransactionId();
        };
    }
    
    private String buildContent(TransactionEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>Notification de Transaction</h2>");
        content.append("<p>Bonjour,</p>");
        content.append("<p>Votre transaction a été traitée avec succès.</p>");
        content.append("<table border='1' cellpadding='10'>");
        content.append("<tr><td><strong>ID Transaction:</strong></td><td>").append(event.getTransactionId()).append("</td></tr>");
        content.append("<tr><td><strong>Type:</strong></td><td>").append(event.getType()).append("</td></tr>");
        content.append("<tr><td><strong>Montant:</strong></td><td>").append(event.getAmount()).append(" ").append(event.getCurrency()).append("</td></tr>");
        content.append("<tr><td><strong>Statut:</strong></td><td>").append(event.getStatus()).append("</td></tr>");
        content.append("<tr><td><strong>Date:</strong></td><td>").append(event.getTimestamp()).append("</td></tr>");
        content.append("</table>");
        content.append("<p>Merci de votre confiance.</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    private Notification.NotificationType determineNotificationType(TransactionEvent event) {
        if (event.getRecipientEmail() != null && event.getFcmToken() != null) {
            return Notification.NotificationType.BOTH;
        } else if (event.getFcmToken() != null) {
            return Notification.NotificationType.PUSH;
        } else {
            return Notification.NotificationType.EMAIL;
        }
    }
}
