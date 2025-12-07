package com.willbank.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a new customer is created
 * Will be consumed by notification service (future RabbitMQ integration)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreatedEvent {

    private Long customerId;
    private String email;
    private String name;
    private String surname;

    /**
     * Temporary password in PLAIN TEXT
     * Only in event, immediately sent to customer via email
     */
    private String temporaryPassword;

    private LocalDateTime createdAt;
}
