package com.willbank.composite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Account with recent transactions (aggregated DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Account information with recent transactions")
public class AccountWithTransactions {

    @Schema(description = "Account details")
    private AccountResponse account;

    @Schema(description = "Recent transactions (last 5-10)")
    private List<TransactionResponse> recentTransactions;
}
