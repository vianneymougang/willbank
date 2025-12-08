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
 * Published by transaction-service when a transaction completes successfully
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long transactionId;
    private Long accountId;
    private Long destinationAccountId; // Null for DEPOSIT/WITHDRAWAL
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
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
