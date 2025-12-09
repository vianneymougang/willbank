package com.willbank.transaction.feign;

import com.willbank.transaction.feign.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Feign Client for Account Service
 * Enables synchronous communication with account-service via Eureka
 * All calls are to internal endpoints (/internal/*) which bypass authentication
 */
@FeignClient(name = "account-service")
public interface AccountClient {

        /**
         * Get account details by ID
         * Internal endpoint - no authentication required
         * 
         * @param id Account ID
         * @return Account details
         * @throws feign.FeignException if account not found (404) or other errors
         */
        @GetMapping("/internal/accounts/{id}")
        AccountResponse getAccountById(@PathVariable("id") Long id);

        /**
         * Update account balance (add or subtract amount)
         * 
         * @param accountId Account ID
         * @param amount    Amount to add (positive) or subtract (negative)
         * @return Updated account
         */
        @PutMapping("/internal/accounts/{accountId}/balance")
        AccountResponse updateBalance(@PathVariable Long accountId, @RequestParam BigDecimal amount);

        /**
         * Check if account has sufficient balance for a withdrawal/transfer
         * Internal endpoint
         * 
         * @param id     Account ID
         * @param amount Amount to check
         * @return true if balance >= amount, false otherwise
         * @throws feign.FeignException if account not found (404)
         */
        @GetMapping("/internal/accounts/{id}/has-sufficient-balance")
        Boolean hasSufficientBalance(
                        @PathVariable("id") Long id,
                        @RequestParam("amount") BigDecimal amount);

        /**
         * Validate account exists and is active
         * Internal endpoint
         * 
         * @param id Account ID
         * @return true if account exists and status is ACTIVE
         * @throws feign.FeignException if account not found (404)
         */
        @GetMapping("/internal/accounts/{id}/is-active")
        Boolean isAccountActive(@PathVariable("id") Long id);
}
