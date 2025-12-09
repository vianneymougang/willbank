package com.willbank.transaction.dto;

import com.willbank.transaction.domain.TransactionStatus;
import com.willbank.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Response DTO
 * Used to return transaction information to clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    /**
     * Transaction ID
     */
    private Long id;

    /**
     * Source account ID
     */
    private Long accountId;

    /**
     * Destination account ID (for transfers)
     */
    private Long destinationAccountId;

    /**
     * Type of transaction
     */
    private TransactionType type;

    /**
     * Transaction amount
     */
    private BigDecimal amount;

    /**
     * Current status
     */
    private TransactionStatus status;

    /**
     * Account balance before transaction
     */
    private BigDecimal balanceBefore;

    /**
     * Account balance after transaction
     */
    private BigDecimal balanceAfter;

    /**
     * Transaction description
     */
    private String description;

    /**
     * Failure reason (if status is FAILED)
     */
    private String failureReason;

    /**
     * When transaction was created
     */
    private LocalDateTime createdAt;

    /**
     * When transaction was last updated
     */
    private LocalDateTime updatedAt;
}
