package com.willbank.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event for transaction completion
 * Will be published by transaction-service (to be implemented)
 * 
 * NOTE: Infrastructure prepared, implementation deferred until
 * transaction-service exists
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long transactionId;
    private Long customerId;
    private String accountNumber;
    private String transactionType; // TRANSFER, DEPOSIT, WITHDRAWAL
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime completedAt;
}
