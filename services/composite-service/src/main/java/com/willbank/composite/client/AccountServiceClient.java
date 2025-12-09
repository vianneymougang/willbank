package com.willbank.composite.client;

import com.willbank.composite.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign Client for Account Service
 * Calls internal endpoints that bypass authentication
 */
@FeignClient(name = "account-service")
public interface AccountServiceClient {

    /**
     * Get accounts by customer ID
     * 
     * @param customerId Customer ID
     * @return List of customer accounts
     */
    @GetMapping("/internal/accounts/customer/{customerId}")
    List<AccountResponse> getAccountsByCustomerId(@PathVariable Long customerId);

    /**
     * Get account by ID
     * 
     * @param id Account ID
     * @return Account information
     */
    @GetMapping("/internal/accounts/{id}")
    AccountResponse getAccountById(@PathVariable Long id);
}
