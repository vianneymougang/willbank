package com.willbank.notificationservice.consumer;

import com.willbank.notificationservice.event.AccountCreatedEvent;
import com.willbank.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Consumer for AccountCreatedEvent
 * Listens for account creation events and sends confirmation emails
 * 
 * NOTE: Will be activated when account-service starts publishing events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountCreatedEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "willbank.account.created")
    public void handleAccountCreated(AccountCreatedEvent event) {
        log.info("📨 Received AccountCreatedEvent for account: {} (customer: {})",
                event.getAccountNumber(), event.getCustomerId());

        try {
            // TODO: Get customer email from user-service via RestTemplate or Feign
            // For now, we'll need the email from the event or fetch it
            // This will be implemented when account-service integration is complete

            log.info("✅ Successfully processed AccountCreatedEvent for account: {}",
                    event.getAccountNumber());

            log.warn("⚠️  Account creation email not sent - customer email retrieval not yet implemented");

        } catch (Exception e) {
            log.error("❌ Failed to process AccountCreatedEvent for account {}: {}",
                    event.getAccountNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to process account created event", e);
        }
    }
}
