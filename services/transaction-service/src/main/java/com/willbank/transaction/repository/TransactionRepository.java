package com.willbank.transaction.repository;

import com.willbank.transaction.domain.Transaction;
import com.willbank.transaction.domain.TransactionStatus;
import com.willbank.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Transaction Entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific account
     * Useful for account transaction history
     */
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    /**
     * Find all transactions for a specific account with pagination
     */
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    /**
     * Find transaction by idempotency key
     * Used to prevent duplicate transaction processing
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all transactions by account and status
     */
    List<Transaction> findByAccountIdAndStatus(Long accountId, TransactionStatus status);

    /**
     * Find all transactions by account and type
     */
    List<Transaction> findByAccountIdAndType(Long accountId, TransactionType type);

    /**
     * Find all transactions for an account within a date range
     */
    List<Transaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Check if a transaction with given idempotency key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find all transactions where account is either source or destination
     * Useful for complete transaction history including transfers
     */
    List<Transaction> findByAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
            Long accountId,
            Long destinationAccountId);
}
