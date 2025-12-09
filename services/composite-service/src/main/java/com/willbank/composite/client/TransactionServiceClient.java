package com.willbank.composite.client;

import com.willbank.composite.dto.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Feign Client for Transaction Service
 * Calls internal endpoints that bypass authentication
 */
@FeignClient(name = "transaction-service")
public interface TransactionServiceClient {

    /**
     * Get paginated transactions for an account
     * 
     * @param accountId     Account ID
     * @param page          Page number
     * @param size          Page size
     * @param sortBy        Sort field
     * @param sortDirection Sort direction (ASC/DESC)
     * @return Paginated transactions
     */
    @GetMapping("/internal/transactions/account/{accountId}/paginated")
    Page<TransactionResponse> getTransactionsByAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection);

    /**
     * Get transactions by date range
     * 
     * @param accountId Account ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of transactions in date range
     */
    @GetMapping("/internal/transactions/account/{accountId}/date-range")
    List<TransactionResponse> getTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate);

    /**
     * Get all transactions for an account
     * 
     * @param accountId Account ID
     * @return List of all transactions
     */
    @GetMapping("/internal/transactions/account/{accountId}")
    List<TransactionResponse> getAllTransactionsForAccount(@PathVariable Long accountId);
}
