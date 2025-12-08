package com.willbank.transaction.dto;

import com.willbank.transaction.domain.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Transaction Request DTO
 * Used for creating new transactions (deposit, withdrawal, transfer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    /**
     * Source account ID (required for all transaction types)
     */
    @NotNull(message = "Account ID is required")
    private Long accountId;

    /**
     * Destination account ID (required only for TRANSFER type)
     */
    private Long destinationAccountId;

    /**
     * Type of transaction
     */
    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    /**
     * Transaction amount (must be positive)
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal amount;

    /**
     * Optional description for the transaction
     */
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    /**
     * Optional idempotency key to prevent duplicate processing
     * If provided, the same key cannot be used for multiple transactions
     */
    @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
    private String idempotencyKey;
}
