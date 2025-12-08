package com.willbank.account.controller;

import com.willbank.account.dto.AccountResponse;
import com.willbank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Internal Account Controller
 * Exposes internal endpoints for service-to-service communication
 * These endpoints bypass authentication and are NOT exposed through API Gateway
 * 
 * Security: /internal/** is whitelisted in SecurityConfig
 */
@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
@Slf4j
public class InternalAccountController {

    private final AccountService accountService;

    /**
     * Get account by ID (internal endpoint)
     * Used by transaction-service to fetch account details
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        log.debug("Internal API: Get account {}", id);
        AccountResponse response = accountService.getAccountByIdInternal(id); // Use internal method
        return ResponseEntity.ok(response);
    }

    /**
     * Update account balance (internal endpoint)
     * Used by transaction-service for atomic balance updates
     * 
     * @param id     Account ID
     * @param amount Amount to add (positive) or subtract (negative)
     */
    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        log.info("Internal API: Update balance for account {} by {}", id, amount);
        AccountResponse response = accountService.updateBalanceInternal(id, amount); // Use internal method
        return ResponseEntity.ok(response);
    }

    /**
     * Check if account has sufficient balance (internal endpoint)
     * Used by transaction-service before processing withdrawals/transfers
     */
    @GetMapping("/{id}/has-sufficient-balance")
    public ResponseEntity<Boolean> hasSufficientBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        log.debug("Internal API: Check sufficient balance for account {}, amount: {}", id, amount);
        boolean hasSufficient = accountService.hasSufficientBalanceInternal(id, amount); // Use internal method
        return ResponseEntity.ok(hasSufficient);
    }

    /**
     * Check if account is active (internal endpoint)
     * Used by transaction-service for validation
     */
    @GetMapping("/{id}/is-active")
    public ResponseEntity<Boolean> isAccountActive(@PathVariable Long id) {
        log.debug("Internal API: Check if account {} is active", id);
        boolean isActive = accountService.isAccountActiveInternal(id); // Use internal method
        return ResponseEntity.ok(isActive);
    }
}
