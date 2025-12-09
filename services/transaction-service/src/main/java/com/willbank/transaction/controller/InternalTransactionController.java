package com.willbank.transaction.controller;

import com.willbank.transaction.dto.TransactionResponse;
import com.willbank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Internal Transaction Controller
 * Exposes internal endpoints for service-to-service communication
 * These endpoints bypass authentication and are NOT exposed through API Gateway
 * 
 * Security: /internal/** is whitelisted in SecurityConfig
 */
@RestController
@RequestMapping("/internal/transactions")
@RequiredArgsConstructor
@Slf4j
public class InternalTransactionController {

    private final TransactionService transactionService;

    /**
     * Get transaction by ID (internal endpoint)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        log.debug("Internal API: Get transaction {}", id);
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions by account with pagination (internal endpoint)
     */
    @GetMapping("/account/{accountId}/paginated")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccountPaginated(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.debug("Internal API: Get paginated transactions for account {}: page={}, size={}", accountId, page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransactionResponse> responses = transactionService.getTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all transactions by account (internal endpoint)
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable Long accountId) {
        log.debug("Internal API: Get all transactions for account {}", accountId);
        List<TransactionResponse> responses = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get transactions by date range (internal endpoint)
     */
    @GetMapping("/account/{accountId}/date-range")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("Internal API: Get transactions for account {} between {} and {}", accountId, startDate, endDate);
        List<TransactionResponse> responses = transactionService.getTransactionsByDateRange(
                accountId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all transactions including those where account is destination (internal
     * endpoint)
     */
    @GetMapping("/account/{accountId}/all")
    public ResponseEntity<List<TransactionResponse>> getAllTransactionsForAccount(@PathVariable Long accountId) {
        log.debug("Internal API: Get all transactions (including transfers) for account {}", accountId);
        List<TransactionResponse> responses = transactionService.getAllTransactionsForAccount(accountId);
        return ResponseEntity.ok(responses);
    }
}
