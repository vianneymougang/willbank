package com.willbank.transaction.controller;

import com.willbank.transaction.dto.TransactionRequest;
import com.willbank.transaction.dto.TransactionResponse;
import com.willbank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction Controller
 * Handles all transaction-related HTTP requests
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Process a deposit", description = "Adds money to an account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        log.info("Received deposit request for account {}", request.getAccountId());
        TransactionResponse response = transactionService.processDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Process a withdrawal", description = "Removes money from an account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody TransactionRequest request) {
        log.info("Received withdrawal request for account {}", request.getAccountId());
        TransactionResponse response = transactionService.processWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Process a transfer", description = "Transfers money from one account to another")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransactionRequest request) {
        log.info("Received transfer request from account {} to account {}",
                request.getAccountId(), request.getDestinationAccountId());
        TransactionResponse response = transactionService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves transaction details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        log.debug("Fetching transaction {}", id);
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transactions by account", description = "Retrieves all transactions for an account")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable Long accountId) {
        log.debug("Fetching transactions for account {}", accountId);
        List<TransactionResponse> responses = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/account/{accountId}/paginated")
    @Operation(summary = "Get paginated transactions", description = "Retrieves transactions with pagination")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccountPaginated(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.debug("Fetching paginated transactions for account {}: page={}, size={}", accountId, page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransactionResponse> responses = transactionService.getTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/account/{accountId}/all")
    @Operation(summary = "Get all transactions including transfers", description = "Retrieves all transactions where account is source or destination")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAllTransactionsForAccount(@PathVariable Long accountId) {
        log.debug("Fetching all transactions (including transfers) for account {}", accountId);
        List<TransactionResponse> responses = transactionService.getAllTransactionsForAccount(accountId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/account/{accountId}/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions within a date range")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("Fetching transactions for account {} between {} and {}", accountId, startDate, endDate);
        List<TransactionResponse> responses = transactionService.getTransactionsByDateRange(
                accountId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }
}
