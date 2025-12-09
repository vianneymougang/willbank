package com.willbank.transaction.exception;

/**
 * Exception thrown when a transaction fails validation
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
