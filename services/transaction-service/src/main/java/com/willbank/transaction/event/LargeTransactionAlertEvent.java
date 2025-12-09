package com.willbank.transaction.event;

import com.willbank.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a large transaction is detected
 * Consumed by notification-service for fraud alerts or admin notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LargeTransactionAlertEvent {

    private Long transactionId;
    private Long accountId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal threshold;
    private LocalDateTime occurredAt;

    // Metadata for notification
    private String customerEmail;
    private String accountNumber;
    private Long customerId;
}
