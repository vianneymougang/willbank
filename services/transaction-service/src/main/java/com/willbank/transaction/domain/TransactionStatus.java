package com.willbank.transaction.domain;

/**
 * Transaction Status Enum
 * Tracks the lifecycle status of a transaction
 */
public enum TransactionStatus {
    /**
     * Transaction is being processed
     */
    PENDING,

    /**
     * Transaction completed successfully
     */
    COMPLETED,

    /**
     * Transaction failed (contains failure reason in Transaction entity)
     */
    FAILED
}
