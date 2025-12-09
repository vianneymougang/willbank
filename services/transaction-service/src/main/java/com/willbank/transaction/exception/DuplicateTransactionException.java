package com.willbank.transaction.exception;

/**
 * Exception thrown when attempting to process a duplicate transaction
 * (same idempotency key already processed)
 */
public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(String message) {
        super(message);
    }

    public static DuplicateTransactionException forIdempotencyKey(String idempotencyKey) {
        return new DuplicateTransactionException(
                String.format("Transaction with idempotency key '%s' already exists", idempotencyKey));
    }
}
