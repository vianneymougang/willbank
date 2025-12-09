package com.willbank.notificationservice.consumer;

import com.willbank.notificationservice.config.RabbitMQConfig;
import com.willbank.notificationservice.event.LargeTransactionAlertEvent;
import com.willbank.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for Large Transaction Alert Events
 * Listens to large transaction alerts and sends notifications to admins and
 * customers
 * Used for fraud detection and compliance monitoring
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LargeTransactionAlertEventConsumer {

    private final EmailService emailService;

    /**
     * Handles large transaction alert events
     * Sends alert emails to both customer and admin team
     * 
     * @param event Large transaction alert event from transaction-service
     */
    @RabbitListener(queues = RabbitMQConfig.LARGE_TRANSACTION_ALERT_QUEUE)
    public void handleLargeTransactionAlert(LargeTransactionAlertEvent event) {
        try {
            log.warn(
                    "🚨 Received LargeTransactionAlertEvent: transactionId={}, amount={} (threshold: {}), accountId={}",
                    event.getTransactionId(), event.getAmount(), event.getThreshold(), event.getAccountId());

            // Send large transaction alert via EmailService (customer + admin)
            emailService.sendLargeTransactionAlert(
                    event.getCustomerId(),
                    event.getCustomerEmail(),
                    event.getTransactionId(),
                    event.getType(),
                    event.getAmount(),
                    event.getThreshold(),
                    event.getAccountNumber(),
                    event.getOccurredAt());

            log.info("✅ Large transaction alert emails sent successfully for transaction {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("❌ Error processing LargeTransactionAlertEvent for transaction {}: {}",
                    event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process large transaction alert", e);
        }
    }
}
