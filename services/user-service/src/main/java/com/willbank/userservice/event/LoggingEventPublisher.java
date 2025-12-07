package com.willbank.userservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of EventPublisher
 * Logs events instead of sending to RabbitMQ
 * 
 * TODO: Replace with RabbitTemplate when notification service is ready
 */
@Component
@Slf4j
public class LoggingEventPublisher implements EventPublisher {

    @Override
    public void publishCustomerCreated(CustomerCreatedEvent event) {
        log.info("📧 EVENT PUBLISHED (stub): CustomerCreated - Customer ID: {}, Email: {}, Name: {} {}",
                event.getCustomerId(),
                event.getEmail(),
                event.getName(),
                event.getSurname());
        log.info("    ⚠️  Temporary password generated (would be sent via email in production)");
        log.info("    TODO: Implement RabbitMQ producer to send to notification service");
    }
}
