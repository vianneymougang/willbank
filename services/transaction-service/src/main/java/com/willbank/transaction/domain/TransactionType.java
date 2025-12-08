package com.willbank.transaction.domain;

/**
 * Transaction Type Enum
 * Defines the different types of banking transactions
 */
public enum TransactionType {
    /**
     * Deposit - Money added to an account
     */
    DEPOSIT,

    /**
     * Withdrawal - Money removed from an account
     */
    WITHDRAWAL,

    /**
     * Transfer - Money transferred from one account to another (debit from source
     * account)
     */
    TRANSFER
}
