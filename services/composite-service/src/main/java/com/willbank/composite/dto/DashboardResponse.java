package com.willbank.composite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard Response DTO (aggregated customer dashboard data)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Customer dashboard with user info, accounts, and recent transactions")
public class DashboardResponse {

    @Schema(description = "User information")
    private UserResponse user;

    @Schema(description = "List of customer accounts with recent transactions")
    private List<AccountWithTransactions> accounts;
}
