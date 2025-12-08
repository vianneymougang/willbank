package com.willbank.transaction.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Response DTO for Feign Client
 * Mirrors the AccountResponse from account-service
 * Used for inter-service communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private Long customerId;
    private String accountNumber;
    private String type; // AccountType as String (SAVINGS, CHECKING, etc.)
    private BigDecimal balance;
    private String status; // AccountStatus as String (ACTIVE, BLOCKED, CLOSED)
    private String label;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
