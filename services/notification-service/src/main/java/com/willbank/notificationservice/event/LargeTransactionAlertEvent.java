package com.willbank.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event received when a large transaction is detected
 * Published by transaction-service for fraud monitoring
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LargeTransactionAlertEvent {

    private Long transactionId;
    private Long accountId;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    private BigDecimal amount;
    private BigDecimal threshold;
    private LocalDateTime occurredAt;

    // Metadata for notification
    private String customerEmail;
    private String accountNumber;
    private Long customerId;
}
