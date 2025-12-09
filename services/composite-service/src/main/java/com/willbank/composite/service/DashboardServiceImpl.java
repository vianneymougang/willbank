package com.willbank.composite.service;

import com.willbank.composite.client.AccountServiceClient;
import com.willbank.composite.client.TransactionServiceClient;
import com.willbank.composite.client.UserServiceClient;
import com.willbank.composite.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard Service Implementation
 * Aggregates data from user-service, account-service, and transaction-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserServiceClient userServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getDashboardFallback")
    @Retry(name = "userService")
    public DashboardResponse getCustomerDashboard(Long customerId) {
        log.info("Fetching dashboard for customer ID: {}", customerId);

        // Step 1: Fetch user information
        UserResponse user = getUserInfo(customerId);

        // Step 2: Fetch customer accounts
        List<AccountResponse> accounts = getCustomerAccounts(customerId);

        // Step 3: For each account, fetch recent transactions (last 10)
        List<AccountWithTransactions> accountsWithTransactions = accounts.stream()
                .map(account -> {
                    List<TransactionResponse> recentTransactions = getRecentTransactions(account.getId(), 10);
                    return AccountWithTransactions.builder()
                            .account(account)
                            .recentTransactions(recentTransactions)
                            .build();
                })
                .collect(Collectors.toList());

        // Step 4: Build and return dashboard response
        DashboardResponse dashboard = DashboardResponse.builder()
                .user(user)
                .accounts(accountsWithTransactions)
                .build();

        log.info("Dashboard fetched successfully for customer ID: {} with {} accounts", customerId, accounts.size());
        return dashboard;
    }

    /**
     * Get user information from user-service
     */
    private UserResponse getUserInfo(Long customerId) {
        log.debug("Fetching user info for customer ID: {}", customerId);
        return userServiceClient.getUserById(customerId);
    }

    /**
     * Get customer accounts from account-service
     */
    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountsFallback")
    @Retry(name = "accountService")
    private List<AccountResponse> getCustomerAccounts(Long customerId) {
        log.debug("Fetching accounts for customer ID: {}", customerId);
        return accountServiceClient.getAccountsByCustomerId(customerId);
    }

    /**
     * Get recent transactions for an account from transaction-service
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsFallback")
    @Retry(name = "transactionService")
    private List<TransactionResponse> getRecentTransactions(Long accountId, int limit) {
        log.debug("Fetching recent {} transactions for account ID: {}", limit, accountId);

        try {
            Page<TransactionResponse> transactionsPage = transactionServiceClient.getTransactionsByAccount(
                    accountId, 0, limit, "createdAt", "DESC");
            return transactionsPage.getContent();
        } catch (Exception e) {
            log.warn("Failed to fetch transactions for account {}: {}", accountId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fallback method for dashboard
     */
    @SuppressWarnings("unused")
    private DashboardResponse getDashboardFallback(Long customerId, Exception ex) {
        log.error("Fallback triggered for dashboard of customer {}: {}", customerId, ex.getMessage());

        // Return minimal dashboard with empty data
        return DashboardResponse.builder()
                .user(UserResponse.builder()
                        .id(customerId)
                        .name("Service Unavailable")
                        .build())
                .accounts(new ArrayList<>())
                .build();
    }

    /**
     * Fallback method for accounts
     */
    @SuppressWarnings("unused")
    private List<AccountResponse> getAccountsFallback(Long customerId, Exception ex) {
        log.error("Fallback triggered for accounts of customer {}: {}", customerId, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for transactions
     */
    @SuppressWarnings("unused")
    private List<TransactionResponse> getTransactionsFallback(Long accountId, int limit, Exception ex) {
        log.error("Fallback triggered for transactions of account {}: {}", accountId, ex.getMessage());
        return Collections.emptyList();
    }
}
