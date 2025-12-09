package com.willbank.userservice.event;

import com.willbank.userservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ implementation of EventPublisher
 * Publishes events to RabbitMQ for consumption by notification-service
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishCustomerCreated(CustomerCreatedEvent event) {
        try {
            log.info("📧 Publishing CustomerCreatedEvent to RabbitMQ for customer: {} ({})",
                    event.getEmail(), event.getCustomerId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CUSTOMER_EXCHANGE,
                    RabbitMQConfig.CUSTOMER_CREATED_ROUTING_KEY,
                    event);

            log.info("✅ Successfully published CustomerCreatedEvent to exchange: {}, routing key: {}",
                    RabbitMQConfig.CUSTOMER_EXCHANGE,
                    RabbitMQConfig.CUSTOMER_CREATED_ROUTING_KEY);

        } catch (Exception e) {
            log.error("❌ Failed to publish CustomerCreatedEvent for customer {}: {}",
                    event.getEmail(), e.getMessage(), e);
            // In production, consider:
            // - Retry mechanism (Spring Retry)
            // - Fallback to direct notification
            // - Alert monitoring system
        }
    }
}
