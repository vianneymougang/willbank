package com.willbank.transaction.event;

import com.willbank.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a transaction is successfully completed
 * Consumed by notification-service to send confirmation emails/notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCompletedEvent {

    private Long transactionId;
    private Long accountId;
    private Long destinationAccountId; // Null for DEPOSIT/WITHDRAWAL
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String description;
    private LocalDateTime completedAt;

    // Account/Customer info for notification
    private String accountNumber;
    private String accountType; // SAVINGS, CHECKING, etc.
    private String customerEmail;
    private String customerFirstName;
    private Long customerId;
}
