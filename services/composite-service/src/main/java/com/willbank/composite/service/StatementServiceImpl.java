package com.willbank.composite.service;

import com.willbank.composite.client.AccountServiceClient;
import com.willbank.composite.client.TransactionServiceClient;
import com.willbank.composite.dto.AccountResponse;
import com.willbank.composite.dto.AccountStatementResponse;
import com.willbank.composite.dto.TransactionResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Statement Service Implementation
 * Provides account statements with transaction summaries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatementServiceImpl implements StatementService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    @Override
    @CircuitBreaker(name = "accountService", fallbackMethod = "getStatementFallback")
    @Retry(name = "accountService")
    public AccountStatementResponse getAccountStatement(Long accountId, LocalDateTime start, LocalDateTime end) {
        log.info("Generating statement for account ID: {} from {} to {}", accountId, start, end);

        // Step 1: Fetch account details
        AccountResponse account = accountServiceClient.getAccountById(accountId);

        // Step 2: Fetch transactions in date range
        List<TransactionResponse> transactions = getTransactionsInRange(accountId, start, end);

        // Step 3: Calculate totals
        BigDecimal totalDebits = calculateTotalDebits(transactions);
        BigDecimal totalCredits = calculateTotalCredits(transactions);

        // Step 4: Build statement response
        AccountStatementResponse statement = AccountStatementResponse.builder()
                .account(account)
                .transactions(transactions)
                .startDate(start)
                .endDate(end)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .build();

        log.info("Statement generated for account ID: {} with {} transactions", accountId, transactions.size());
        return statement;
    }

    /**
     * Fetch transactions in date range from transaction-service
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsFallback")
    @Retry(name = "transactionService")
    private List<TransactionResponse> getTransactionsInRange(Long accountId, LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching transactions for account {} from {} to {}", accountId, start, end);
        return transactionServiceClient.getTransactionsByDateRange(accountId, start, end);
    }

    /**
     * Calculate total debits (withdrawals, transfers out)
     */
    private BigDecimal calculateTotalDebits(List<TransactionResponse> transactions) {
        return transactions.stream()
                .filter(tx -> "WITHDRAWAL".equals(tx.getType()) ||
                        ("TRANSFER".equals(tx.getType()) && tx.getAccountId() != null))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total credits (deposits, transfers in)
     */
    private BigDecimal calculateTotalCredits(List<TransactionResponse> transactions) {
        return transactions.stream()
                .filter(tx -> "DEPOSIT".equals(tx.getType()) ||
                        ("TRANSFER".equals(tx.getType()) && tx.getDestinationAccountId() != null))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Fallback method for statement
     */
    @SuppressWarnings("unused")
    private AccountStatementResponse getStatementFallback(Long accountId, LocalDateTime start, LocalDateTime end,
            Exception ex) {
        log.error("Fallback triggered for statement of account {}: {}", accountId, ex.getMessage());

        return AccountStatementResponse.builder()
                .account(AccountResponse.builder()
                        .id(accountId)
                        .accountNumber("Service Unavailable")
                        .balance(BigDecimal.ZERO)
                        .build())
                .transactions(Collections.emptyList())
                .startDate(start)
                .endDate(end)
                .totalDebits(BigDecimal.ZERO)
                .totalCredits(BigDecimal.ZERO)
                .build();
    }

    /**
     * Fallback method for transactions
     */
    @SuppressWarnings("unused")
    private List<TransactionResponse> getTransactionsFallback(Long accountId, LocalDateTime start, LocalDateTime end,
            Exception ex) {
        log.error("Fallback triggered for transactions of account {}: {}", accountId, ex.getMessage());
        return Collections.emptyList();
    }
}
