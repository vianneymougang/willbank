package com.willbank.account.service;

import com.willbank.account.domain.AccountStatus;
import com.willbank.account.dto.AccountRequest;
import com.willbank.account.dto.AccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    /**
     * Create a new account
     */
    AccountResponse createAccount(AccountRequest request);

    /**
     * Get account by ID
     */
    AccountResponse getAccountById(Long id);

    /**
     * Get all accounts
     */
    List<AccountResponse> getAllAccounts();

    /**
     * Get all accounts for a customer
     */
    List<AccountResponse> getAccountsByCustomerId(Long customerId);

    /**
     * Update account
     */
    AccountResponse updateAccount(Long id, AccountRequest request);

    /**
     * Delete account
     */
    void deleteAccount(Long id);

    /**
     * Update account status
     */
    AccountResponse updateAccountStatus(Long id, AccountStatus status);

    // Internal methods for service-to-service communication (no auth checks)
    AccountResponse getAccountByIdInternal(Long id);

    AccountResponse updateBalanceInternal(Long id, BigDecimal amount);

    boolean hasSufficientBalanceInternal(Long id, BigDecimal amount);

    boolean isAccountActiveInternal(Long id);

    /**
     * Get account balance
     */
    BigDecimal getAccountBalance(Long id);

    // ============================================
    // Internal Methods for Transaction Service
    // ============================================

    /**
     * Update account balance (add or subtract amount)
     * Used by transaction-service for atomic balance updates
     * 
     * @param id     Account ID
     * @param amount Amount to add (positive) or subtract (negative)
     * @return Updated account
     */
    AccountResponse updateBalance(Long id, BigDecimal amount);

    /**
     * Check if account has sufficient balance
     * 
     * @param id     Account ID
     * @param amount Amount to check
     * @return true if balance >= amount
     */
    boolean hasSufficientBalance(Long id, BigDecimal amount);

    /**
     * Check if account is active
     * 
     * @param id Account ID
     * @return true if account status is ACTIVE
     */
    boolean isAccountActive(Long id);
}
