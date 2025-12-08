package com.willbank.notificationservice.consumer;

import com.willbank.notificationservice.config.RabbitMQConfig;
import com.willbank.notificationservice.event.TransactionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for Transaction Completed Events
 * Listens to transaction completion events and sends email notifications to
 * customers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCompletedEventConsumer {

    private final com.willbank.notificationservice.service.EmailService emailService;

    /**
     * Handles transaction completed events
     * Sends confirmation email to customer about their transaction
     * 
     * @param event Transaction completed event from transaction-service
     */
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_COMPLETED_QUEUE)
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        try {
            log.info("📩 Received TransactionCompletedEvent: transactionId={}, type={}, amount={}, accountId={}",
                    event.getTransactionId(), event.getType(), event.getAmount(), event.getAccountId());

            // Send transaction confirmation email
            emailService.sendTransactionCompletedEmail(
                    event.getCustomerId(),
                    event.getCustomerEmail(),
                    event.getTransactionId(),
                    event.getType(),
                    event.getAmount(),
                    event.getNewBalance(),
                    event.getAccountNumber(),
                    event.getDescription(),
                    event.getCompletedAt(),
                    event.getDestinationAccountId(),
                    event.getCustomerFirstName(),
                    event.getAccountType());

            log.info("✅ Transaction notification email sent successfully for transaction {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("❌ Error processing TransactionCompletedEvent for transaction {}: {}",
                    event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process transaction completed event", e);
        }
    }
}
