package com.willbank.notificationservice.consumer;

import com.willbank.notificationservice.event.CustomerCreatedEvent;
import com.willbank.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Consumer for CustomerCreatedEvent
 * Listens for customer creation events and sends welcome emails
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerCreatedEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "willbank.customer.created")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("📨 Received CustomerCreatedEvent for customer: {} ({})",
                event.getEmail(), event.getCustomerId());

        try {
            // Send welcome email with temporary password
            emailService.sendWelcomeEmail(
                    event.getCustomerId(),
                    event.getEmail(),
                    event.getName(),
                    event.getSurname(),
                    event.getTemporaryPassword());

            log.info("✅ Successfully processed CustomerCreatedEvent for: {}", event.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to process CustomerCreatedEvent for customer {}: {}",
                    event.getEmail(), e.getMessage(), e);
            // Exception will be caught by RabbitMQ and sent to DLQ after retries
            throw new RuntimeException("Failed to process customer created event", e);
        }
    }
}
