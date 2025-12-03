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

    /**
     * Get account balance
     */
    BigDecimal getAccountBalance(Long id);
}
