package com.willbank.transaction.exception;

/**
 * Exception thrown when an account has insufficient balance for a transaction
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(Long accountId, java.math.BigDecimal required, java.math.BigDecimal available) {
        super(String.format("Insufficient balance in account %d. Required: %s, Available: %s",
                accountId, required, available));
    }
}
