package com.willbank.transaction.service;

import com.willbank.transaction.dto.TransactionRequest;
import com.willbank.transaction.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction Service Interface
 * Defines business logic for transaction management
 */
public interface TransactionService {

    /**
     * Process a deposit transaction
     * 
     * @param request Transaction request
     * @return Created transaction
     */
    TransactionResponse processDeposit(TransactionRequest request);

    /**
     * Process a withdrawal transaction
     * 
     * @param request Transaction request
     * @return Created transaction
     */
    TransactionResponse processWithdrawal(TransactionRequest request);

    /**
     * Process a transfer transaction
     * 
     * @param request Transaction request
     * @return Created transaction
     */
    TransactionResponse processTransfer(TransactionRequest request);

    /**
     * Get transaction by ID
     * 
     * @param id Transaction ID
     * @return Transaction details
     */
    TransactionResponse getTransactionById(Long id);

    /**
     * Get all transactions for an account
     * 
     * @param accountId Account ID
     * @return List of transactions
     */
    List<TransactionResponse> getTransactionsByAccountId(Long accountId);

    /**
     * Get transactions for an account with pagination
     * 
     * @param accountId Account ID
     * @param pageable  Pagination parameters
     * @return Page of transactions
     */
    Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable);

    /**
     * Get all transactions (source or destination) for an account
     * Includes transfers in both directions
     * 
     * @param accountId Account ID
     * @return List of transactions
     */
    List<TransactionResponse> getAllTransactionsForAccount(Long accountId);

    /**
     * Get transactions by account and date range
     * 
     * @param accountId Account ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of transactions
     */
    List<TransactionResponse> getTransactionsByDateRange(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
