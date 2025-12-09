package com.willbank.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event published when a new customer is created
 * Consumed from user-service via RabbitMQ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

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
