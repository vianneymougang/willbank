package com.willbank.transaction.exception;

/**
 * Exception thrown when an account is not in ACTIVE status
 */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String message) {
        super(message);
    }

    public AccountNotActiveException(Long accountId, String status) {
        super(String.format("Account %d is not active. Current status: %s", accountId, status));
    }
}
