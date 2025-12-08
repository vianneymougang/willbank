package com.willbank.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity
 * Represents a banking transaction (deposit, withdrawal, or transfer)
 * Each transaction is immutable once completed or failed
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_account_id", columnList = "account_id"),
        @Index(name = "idx_destination_account_id", columnList = "destination_account_id"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Source account ID (the account being debited or credited)
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * Destination account ID (for transfers only, null for deposits/withdrawals)
     */
    @Column(name = "destination_account_id")
    private Long destinationAccountId;

    /**
     * Type of transaction
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /**
     * Transaction amount (always positive)
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Current status of the transaction
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    /**
     * Account balance before this transaction
     */
    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    /**
     * Account balance after this transaction
     */
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    /**
     * Optional description/memo for the transaction
     */
    @Column(length = 255)
    private String description;

    /**
     * Idempotency key to prevent duplicate transactions
     * Should be unique per transaction request
     */
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    /**
     * Reason for transaction failure (populated if status = FAILED)
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * Timestamp when transaction was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when transaction was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - set default values before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    /**
     * JPA lifecycle callback - update timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
