package com.willbank.userservice.event;

/**
 * Interface for publishing domain events
 * Current implementation: logging stub
 * Future implementation: RabbitMQ producer
 */
public interface EventPublisher {

    /**
     * Publish customer created event
     * Will be sent to notification service for email delivery
     */
    void publishCustomerCreated(CustomerCreatedEvent event);
}
