package com.willbank.account.controller;

import com.willbank.account.domain.AccountStatus;
import com.willbank.account.dto.AccountRequest;
import com.willbank.account.dto.AccountResponse;
import com.willbank.account.security.AuthorizationService;
import com.willbank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    private final AccountService accountService;
    private final AuthorizationService authorizationService;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new bank account for a customer")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        log.info("REST request to create account for customer: {}", request.getCustomerId());
        // Validate: customers can only create accounts for themselves, admins can
        // create for anyone
        authorizationService.validateAccountCreation(request.getCustomerId());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts (ADMIN only)", description = "Retrieves all bank accounts - admin only")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        log.info("REST request to get all accounts");
        List<AccountResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves a specific account by its ID - customers can only see their own")
    public ResponseEntity<AccountResponse> getAccountById(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.info("REST request to get account: {}", id);
        AccountResponse response = accountService.getAccountById(id);
        // Authorization check is done in service layer
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get accounts by customer", description = "Retrieves all accounts for a specific customer")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("REST request to get accounts for customer: {}", customerId);
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account", description = "Updates an existing account")
    public ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "Account ID") @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {
        log.info("REST request to update account: {}", id);
        AccountResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account", description = "Deletes an account (only if balance is zero)")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.info("REST request to delete account: {}", id);
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update account status", description = "Updates the status of an account (ACTIVE, BLOCKED, CLOSED)")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @Parameter(description = "Account ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam AccountStatus status) {
        log.info("REST request to update account status: {} to {}", id, status);
        AccountResponse response = accountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance", description = "Retrieves the current balance of an account - customers can only see their own")
    public ResponseEntity<BigDecimal> getAccountBalance(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        log.info("REST request to get balance for account: {}", id);
        BigDecimal balance = accountService.getAccountBalance(id);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/my-accounts")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my accounts", description = "Retrieves all accounts for the authenticated customer")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        Long customerId = authorizationService.getCurrentUserId();
        log.info("REST request to get my accounts for customer: {}", customerId);
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }
}
